package com.kalibyte.YashTools;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class YashToolBackEndApplication {

	public static void main(String[] args) {
		loadDotEnv();
		runFlywayMigrations();
		SpringApplication.run(YashToolBackEndApplication.class, args);
	}

	private static void loadDotEnv() {
		java.io.File file = new java.io.File(".env");
		if (file.exists()) {
			try (java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.FileReader(file))) {
				String line;
				while ((line = reader.readLine()) != null) {
					line = line.trim();
					if (line.isEmpty() || line.startsWith("#")) continue;
					int eq = line.indexOf('=');
					if (eq > 0) {
						String key = line.substring(0, eq).trim();
						String val = line.substring(eq + 1).trim();
						if (key.startsWith("SPRING_DATASOURCE")) continue;
						String existingProp = System.getProperty(key);
						if (existingProp == null || existingProp.isBlank()) {
							System.setProperty(key, val);
						}
					}
				}
				System.out.println("====== Loaded environment variables from .env ======");
			} catch (Exception e) {
				System.err.println("Failed to load .env file: " + e.getMessage());
			}
		}
	}

	private static void runFlywayMigrations() {
		String url = System.getenv("SPRING_DATASOURCE_URL");
		if (url == null) {
			url = "jdbc:postgresql://localhost:9090/YashTool?useSSL=false";
		}
		String username = System.getenv("SPRING_DATASOURCE_USERNAME");
		if (username == null) {
			username = "postgres";
		}
		String password = System.getenv("SPRING_DATASOURCE_PASSWORD");
		if (password == null) {
			password = "1422";
		}

		System.out.println("====== Running Flyway Migrations from Main ======");
		System.out.println("Database URL: " + url);
		System.out.println("Database Username: " + username);
		try {
			org.flywaydb.core.Flyway flyway = org.flywaydb.core.Flyway.configure()
					.dataSource(url, username, password)
					.baselineOnMigrate(true)
					.baselineVersion("0")
					.locations("classpath:db/migration")
					.outOfOrder(true)
					.load();
			flyway.migrate();
			System.out.println("====== Flyway Migrations Completed Successfully ======");
		} catch (Exception e) {
			System.err.println("====== Flyway Migrations Failed: " + e.getMessage() + " ======");
			e.printStackTrace();
		}
	}
}
