package com.kalibyte.YashTools;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import java.util.List;
import java.util.Map;

@SpringBootTest
public class DbCheck {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    public void checkTables() {
        System.out.println("--- SCHEMA CHECK ---");
        try {
            List<Map<String, Object>> tables = jdbcTemplate.queryForList(
                "SELECT table_name FROM information_schema.tables WHERE table_schema = 'public'"
            );
            for (Map<String, Object> table : tables) {
                System.out.println("TABLE: " + table.get("table_name"));
            }
            
            try {
                List<Map<String, Object>> companies = jdbcTemplate.queryForList("SELECT id, code, name FROM companies");
                for (Map<String, Object> company : companies) {
                    System.out.println("COMPANY: id=" + company.get("id") + ", code=" + company.get("code") + ", name=" + company.get("name"));
                }
            } catch (Exception ex) {
                System.out.println("Error reading companies table: " + ex.getMessage());
            }

            try {
                List<Map<String, Object>> coolantRods = jdbcTemplate.queryForList("SELECT id, category, item, price, active FROM hyperion_coolant_hole_rod_price");
                for (Map<String, Object> rod : coolantRods) {
                    System.out.println("COOLANT_ROD: category=" + rod.get("category") + ", item=" + rod.get("item") + ", price=" + rod.get("price") + ", active=" + rod.get("active"));
                }
            } catch (Exception ex) {
                System.out.println("Error reading hyperion_coolant_hole_rod_price: " + ex.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
