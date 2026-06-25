package com.kalibyte.YashTools;

import org.flywaydb.core.Flyway;

public class FlywayStandaloneRunner {
    public static void main(String[] args) {
        System.out.println("Starting Flyway Standalone Runner...");

        // Defaults
        String url = "jdbc:postgresql://localhost:9090/YashTool?useSSL=false";
        String username = "postgres";
        String password = "1422";

        // Read .env manually
        try (java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.FileReader(".env"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                String[] parts = line.split("=", 2);
                if (parts.length == 2) {
                    String key = parts[0].trim();
                    String val = parts[1].trim();
                    if (key.equals("SPRING_DATASOURCE_URL")) url = val;
                    if (key.equals("SPRING_DATASOURCE_USERNAME")) username = val;
                    if (key.equals("SPRING_DATASOURCE_PASSWORD")) password = val;
                }
            }
        } catch (Exception e) {
            System.out.println("Could not read .env, using default database configuration: " + e.getMessage());
        }

        System.out.println("Connecting to Database URL: " + url);
        System.out.println("Username: " + username);

        try {
            Flyway flyway = Flyway.configure()
                    .dataSource(url, username, password)
                    .cleanDisabled(false)
                    .baselineOnMigrate(true)
                    .baselineVersion("0")
                    .locations("filesystem:src/main/resources/db/migration")
                    .load();

            // System.out.println("Cleaning database...");
            // flyway.clean();

            System.out.println("Running Flyway migrate...");
            flyway.migrate();

            System.out.println("Flyway migration completed successfully!");
        } catch (Exception e) {
            System.err.println("Flyway migration failed:");
            e.printStackTrace();
        }
    }
}
