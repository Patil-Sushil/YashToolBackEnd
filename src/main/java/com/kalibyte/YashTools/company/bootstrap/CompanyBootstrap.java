package com.kalibyte.YashTools.company.bootstrap;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@ConditionalOnProperty(prefix = "app.bootstrap", name = "enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
@Order(1) // Runs early in the startup sequence
public class CompanyBootstrap implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        try {
            log.info("Checking company seeding...");

            UUID ytId = UUID.fromString("a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11");
            UUID swId = UUID.fromString("a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a22");

            LocalDateTime now = LocalDateTime.now();

            // Seed 'YT' (Yash Tools)
            Integer ytCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM companies WHERE id = ? OR code = ?",
                    Integer.class,
                    ytId, "YT"
            );

            if (ytCount == null || ytCount == 0) {
                jdbcTemplate.update(
                        "INSERT INTO companies (id, code, name, gst_number, bank_name, bank_account_no, bank_ifsc, bank_branch, address, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                        ytId, "YT", "Yash Tools", "27AAAAY0000A1Z1", "State Bank of India", "12345678901", "SBIN0000001", "Main Branch", "Industrial Area, Maharashtra",
                        now, now
                );
                log.info("Created default company: YT (Yash Tools)");
            } else {
                log.info("Company YT already exists (by ID or Code).");
            }

            // Seed 'SW' (Swara Enterprises)
            Integer swCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM companies WHERE id = ? OR code = ?",
                    Integer.class,
                    swId, "SW"
            );

            if (swCount == null || swCount == 0) {
                jdbcTemplate.update(
                        "INSERT INTO companies (id, code, name, gst_number, bank_name, bank_account_no, bank_ifsc, bank_branch, address, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                        swId, "SW", "Swara Enterprises", "27AAAAS9999B1Z2", "HDFC Bank", "98765432109", "HDFC0000123", "City Branch", "MIDC, Maharashtra",
                        now, now
                );
                log.info("Created default company: SW (Swara Enterprises)");
            } else {
                log.info("Company SW already exists (by ID or Code).");
            }

            log.info("Company seeding check complete.");
        } catch (Exception e) {
            log.warn("Could not seed companies. Error: {}", e.getMessage());
        }
    }
}
