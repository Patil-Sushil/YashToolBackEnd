package com.kalibyte.YashTools.production.machine.service.impl;

import com.kalibyte.YashTools.common.exception.BusinessException;
import com.kalibyte.YashTools.common.multi_company.CompanyContextHolder;
import com.kalibyte.YashTools.company.entity.Company;
import com.kalibyte.YashTools.production.machine.dto.MachineRequest;
import com.kalibyte.YashTools.production.machine.dto.MachineResponse;
import com.kalibyte.YashTools.production.machine.entity.Machine;
import com.kalibyte.YashTools.production.machine.entity.enums.MachineStatus;
import com.kalibyte.YashTools.production.machine.repository.MachineRepository;
import com.kalibyte.YashTools.production.machine.service.MachineService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MachineServiceImpl implements MachineService {

    private final MachineRepository machineRepository;

    @Override
    @Transactional
    public MachineResponse create(MachineRequest request) {
        UUID companyId = CompanyContextHolder.getCompanyId();
        String companyCode = CompanyContextHolder.getCompanyCode();

        if (machineRepository.existsByCodeAndCompanyId(request.getCode(), companyId)) {
            throw new BusinessException("Machine code already exists: " + request.getCode());
        }

        MachineStatus status = MachineStatus.ACTIVE;
        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            try {
                status = MachineStatus.valueOf(request.getStatus().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new BusinessException("Invalid status: " + request.getStatus());
            }
        }

        Company company = Company.builder().id(companyId).code(companyCode).build();
        Machine machine = Machine.builder()
                .name(request.getName())
                .code(request.getCode())
                .status(status)
                .build();
        machine.setCompany(company);

        Machine saved = machineRepository.save(machine);
        log.info("Machine created successfully: {}", saved.getCode());
        return toResponse(saved);
    }

    @Override
    @Transactional
    public MachineResponse update(UUID id, MachineRequest request) {
        UUID companyId = CompanyContextHolder.getCompanyId();
        Machine machine = machineRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new BusinessException("Machine not found with ID: " + id));

        if (!machine.getCode().equalsIgnoreCase(request.getCode()) 
                && machineRepository.existsByCodeAndCompanyId(request.getCode(), companyId)) {
            throw new BusinessException("Machine code already exists: " + request.getCode());
        }

        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            try {
                machine.setStatus(MachineStatus.valueOf(request.getStatus().toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new BusinessException("Invalid status: " + request.getStatus());
            }
        }

        machine.setName(request.getName());
        machine.setCode(request.getCode());

        Machine saved = machineRepository.save(machine);
        log.info("Machine updated successfully: {}", saved.getCode());
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public MachineResponse getById(UUID id) {
        UUID companyId = CompanyContextHolder.getCompanyId();
        Machine machine = machineRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new BusinessException("Machine not found with ID: " + id));
        return toResponse(machine);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MachineResponse> list(Pageable pageable) {
        UUID companyId = CompanyContextHolder.getCompanyId();
        return machineRepository.findAll((root, query, cb) -> 
                cb.equal(root.get("company").get("id"), companyId), pageable)
                .map(this::toResponse);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        UUID companyId = CompanyContextHolder.getCompanyId();
        Machine machine = machineRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new BusinessException("Machine not found with ID: " + id));
        machineRepository.delete(machine);
        log.info("Machine deleted: {}", machine.getCode());
    }

    private MachineResponse toResponse(Machine m) {
        return MachineResponse.builder()
                .id(m.getId())
                .name(m.getName())
                .code(m.getCode())
                .status(m.getStatus().name())
                .companyId(m.getCompany().getId())
                .build();
    }
}
