package com.kfpcl.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Ensures existing database tables have sufficiently sized columns (LONGTEXT)
 * for images, logos, banners, and URLs across deployments.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseSchemaInitializer implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    private static final List<String> SCHEMA_MIGRATIONS = List.of(
            "ALTER TABLE categories MODIFY COLUMN image_url LONGTEXT",
            "ALTER TABLE categories MODIFY COLUMN icon_url LONGTEXT",
            "ALTER TABLE subcategories MODIFY COLUMN image_url LONGTEXT",
            "ALTER TABLE products MODIFY COLUMN image_url LONGTEXT",
            "ALTER TABLE product_images MODIFY COLUMN image_url LONGTEXT",
            "ALTER TABLE sellers MODIFY COLUMN banner_url LONGTEXT",
            "ALTER TABLE sellers MODIFY COLUMN logo_url LONGTEXT",
            "ALTER TABLE seller_profiles MODIFY COLUMN banner_url LONGTEXT",
            "ALTER TABLE seller_profiles MODIFY COLUMN logo_url LONGTEXT",
            "ALTER TABLE brands MODIFY COLUMN logo_url LONGTEXT",
            "ALTER TABLE message_attachments MODIFY COLUMN file_url LONGTEXT",
            "ALTER TABLE products ADD COLUMN region_of_origin VARCHAR(150)",
            "ALTER TABLE products ADD COLUMN country_of_origin VARCHAR(150)"
    );

    @Override
    public void run(ApplicationArguments args) {
        for (String sql : SCHEMA_MIGRATIONS) {
            try {
                jdbcTemplate.execute(sql);
                log.info("Successfully executed schema update: {}", sql);
            } catch (Exception ex) {
                // Safely log and ignore if table/column does not exist yet or running against non-MySQL dialect
                log.debug("Skipping schema statement '{}': {}", sql, ex.getMessage());
            }
        }
    }
}
