package com.kalibyte.YashTools;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class FlywayMigrate {

    @Value("${spring.datasource.url}")
    private String url;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    @Test
    public void runMigration() {
        System.out.println("Manually running Flyway migration against: " + url);
        try {
            // Delete V7 record to force Flyway to re-run the updated script
//            try (java.sql.Connection conn = java.sql.DriverManager.getConnection(url, username, password);
//                 java.sql.Statement stmt = conn.createStatement()) {
//                stmt.executeUpdate("DELETE FROM flyway_schema_history WHERE version = '7'");
//                System.out.println("Deleted migration version 7 from history table.");
//            } catch (Exception ex) {
//                System.out.println("Error deleting migration history: " + ex.getMessage());
//            }

            Flyway flyway = Flyway.configure()
                    .dataSource(url, username, password)
                    .baselineOnMigrate(true)
                    .baselineVersion("0")
                    .locations("classpath:db/migration")
                    .outOfOrder(true)
                    .load();
            flyway.repair();
            flyway.migrate();
            System.out.println("Flyway migration completed successfully!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
