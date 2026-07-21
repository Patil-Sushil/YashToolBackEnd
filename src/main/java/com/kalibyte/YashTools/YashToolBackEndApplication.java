package com.kalibyte.YashTools;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class YashToolBackEndApplication {

	public static void main(String[] args) {
		runFlywayMigrations();
		SpringApplication.run(YashToolBackEndApplication.class, args);
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
