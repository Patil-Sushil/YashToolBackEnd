package com.kalibyte.YashTools.labors.seeder;

import com.kalibyte.YashTools.labors.advance.dto.AdvanceTransactionRequestDTO;
import com.kalibyte.YashTools.labors.advance.entity.Enum.TransactionType;
import com.kalibyte.YashTools.labors.advance.service.AdvanceService;
import com.kalibyte.YashTools.labors.attendance.dto.AttendanceRequestDTO;
import com.kalibyte.YashTools.labors.attendance.service.AttendanceService;
import com.kalibyte.YashTools.labors.labor.dto.LaborerRequestDTO;
import com.kalibyte.YashTools.labors.labor.dto.LaborerResponseDTO;
import com.kalibyte.YashTools.labors.labor.entity.Enum.LaborRole;
import com.kalibyte.YashTools.labors.labor.entity.Enum.WageType;
import com.kalibyte.YashTools.labors.labor.repository.LaborerRepository;
import com.kalibyte.YashTools.labors.labor.service.LaborerService;
import com.kalibyte.YashTools.labors.payout.dto.WeeklyPayoutRequestDTO;
import com.kalibyte.YashTools.labors.payout.service.WeeklyPayoutService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Component
@RequiredArgsConstructor
@Slf4j
@Order(3)
public class LaborDatabaseSeeder implements CommandLineRunner {

    private final LaborerRepository laborerRepository;
    private final LaborerService laborerService;
    private final AttendanceService attendanceService;
    private final AdvanceService advanceService;
    private final WeeklyPayoutService weeklyPayoutService;

    @Override
    public void run(String... args) throws Exception {
        if (laborerRepository.count() == 0) {
            log.info("Seeding Labor Management data...");

            // 1. Create Laborers
            LaborerResponseDTO cnc1 = laborerService.createLaborer(LaborerRequestDTO.builder()
                    .name("Rahul Sharma")
                    .role(LaborRole.CNC_OPERATOR)
                    .wageType(WageType.DAILY)
                    .dailyWage(new BigDecimal("1200.00"))
                    .phNumber("9876543210")
                    .isActive(true)
                    .build());

            LaborerResponseDTO grinding1 = laborerService.createLaborer(LaborerRequestDTO.builder()
                    .name("Amit Patel")
                    .role(LaborRole.GRINDING_OPERATOR)
                    .wageType(WageType.DAILY)
                    .dailyWage(new BigDecimal("1000.00"))
                    .phNumber("8765432109")
                    .isActive(true)
                    .build());

            LaborerResponseDTO operator1 = laborerService.createLaborer(LaborerRequestDTO.builder()
                    .name("Suresh Kumar")
                    .role(LaborRole.OPERATOR)
                    .wageType(WageType.HOURLY)
                    .hourlyRate(new BigDecimal("150.00"))
                    .phNumber("7654321098")
                    .isActive(true)
                    .build());

            LaborerResponseDTO helper1 = laborerService.createLaborer(LaborerRequestDTO.builder()
                    .name("Vicky Singh")
                    .role(LaborRole.HELPER)
                    .wageType(WageType.PIECE_RATE)
                    .pieceRate(new BigDecimal("20.00"))
                    .phNumber("6543210987")
                    .isActive(true)
                    .build());

            LaborerResponseDTO supervisor1 = laborerService.createLaborer(LaborerRequestDTO.builder()
                    .name("Karan Johar")
                    .role(LaborRole.SUPERVISOR)
                    .wageType(WageType.DAILY)
                    .dailyWage(new BigDecimal("2000.00"))
                    .phNumber("9999888877")
                    .isActive(true)
                    .build());

            // 2. Log 3 days of attendance
            LocalDate today = LocalDate.now();
            for (int i = 1; i <= 3; i++) {
                LocalDate workDate = today.minusDays(i);

                attendanceService.logAttendance(AttendanceRequestDTO.builder()
                        .laborerId(cnc1.getId())
                        .workDate(workDate)
                        .checkInTime(LocalTime.of(9, 0))
                        .checkOutTime(LocalTime.of(18, 0))
                        .build());

                attendanceService.logAttendance(AttendanceRequestDTO.builder()
                        .laborerId(grinding1.getId())
                        .workDate(workDate)
                        .checkInTime(LocalTime.of(9, 0))
                        .checkOutTime(LocalTime.of(18, 0))
                        .build());

                attendanceService.logAttendance(AttendanceRequestDTO.builder()
                        .laborerId(operator1.getId())
                        .workDate(workDate)
                        .checkInTime(LocalTime.of(10, 0))
                        .checkOutTime(LocalTime.of(16, 0))
                        .build());

                attendanceService.logAttendance(AttendanceRequestDTO.builder()
                        .laborerId(helper1.getId())
                        .workDate(workDate)
                        .piecesCompleted(50)
                        .build());

                attendanceService.logAttendance(AttendanceRequestDTO.builder()
                        .laborerId(supervisor1.getId())
                        .workDate(workDate)
                        .checkInTime(LocalTime.of(8, 30))
                        .checkOutTime(LocalTime.of(18, 30))
                        .build());
            }

            // 3. Grant advance
            advanceService.grantAdvance(AdvanceTransactionRequestDTO.builder()
                    .laborerId(cnc1.getId())
                    .transactionDate(today.minusDays(4))
                    .amount(new BigDecimal("1000.00"))
                    .transactionType(TransactionType.GIVEN)
                    .notes("Personal advance")
                    .build());

            advanceService.grantAdvance(AdvanceTransactionRequestDTO.builder()
                    .laborerId(supervisor1.getId())
                    .transactionDate(today.minusDays(2))
                    .amount(new BigDecimal("500.00"))
                    .transactionType(TransactionType.GIVEN)
                    .notes("Festival advance")
                    .build());

            // 4. Generate Weekly Payouts
            LocalDate weekStart = today.minusDays(7);
            LocalDate weekEnd = today;

            weeklyPayoutService.generateWeeklyPayout(WeeklyPayoutRequestDTO.builder()
                    .laborerId(cnc1.getId())
                    .weekStartDate(weekStart)
                    .weekEndDate(weekEnd)
                    .build());

            weeklyPayoutService.generateWeeklyPayout(WeeklyPayoutRequestDTO.builder()
                    .laborerId(grinding1.getId())
                    .weekStartDate(weekStart)
                    .weekEndDate(weekEnd)
                    .build());

            log.info("Labor Management data seeding completed.");
        }
    }
}
