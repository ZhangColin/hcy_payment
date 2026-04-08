package com.aieducenter.config;

import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

/**
 * Flyway 多实例配置类
 *
 * <p>为每个限界上下文配置独立的 Flyway 实例和历史表，实现上下文隔离的数据库迁移管理。
 *
 * <p>每个上下文使用独立的历史表：
 * <ul>
 *   <li>Account: {@code flyway_account_history}</li>
 *   <li>Admin: {@code flyway_admin_history}</li>
 *   <li>Tenant: {@code flyway_tenant_history}</li>
 * </ul>
 *
 * <p>这样每个上下文可以使用自己的版本号序列（V1, V2, V3...），避免版本号冲突。
 */
@Configuration
public class FlywayConfig {

    private static final Logger log = LoggerFactory.getLogger(FlywayConfig.class);

    /**
     * 在应用启动时执行所有 Flyway 迁移
     *
     * <p>使用 @Order(1) 确保在任何其他 Bean 初始化之前执行迁移。
     */
    @Bean
    @Order(1)
    public CommandLineRunner flywayMigrations(DataSource dataSource) {
        return args -> {
            log.info("Starting Flyway migrations with multiple instances...");

            // Account 上下文
            log.info("Migrating Account context...");
            Flyway accountFlyway = Flyway.configure()
                    .dataSource(dataSource)
                    .locations("classpath:db/migration/account")
                    .table("flyway_account_history")
                    .baselineOnMigrate(true)
                    .baselineVersion("0")
                    .load();
            accountFlyway.migrate();
            log.info("Account context migration completed.");

            // Admin 上下文
            log.info("Migrating Admin context...");
            Flyway adminFlyway = Flyway.configure()
                    .dataSource(dataSource)
                    .locations("classpath:db/migration/admin")
                    .table("flyway_admin_history")
                    .baselineOnMigrate(true)
                    .baselineVersion("0")
                    .load();
            adminFlyway.migrate();
            log.info("Admin context migration completed.");

            // Tenant 上下文
            log.info("Migrating Tenant context...");
            Flyway tenantFlyway = Flyway.configure()
                    .dataSource(dataSource)
                    .locations("classpath:db/migration/tenant")
                    .table("flyway_tenant_history")
                    .baselineOnMigrate(true)
                    .baselineVersion("0")
                    .load();
            tenantFlyway.migrate();
            log.info("Tenant context migration completed.");

            log.info("All Flyway migrations completed successfully.");
        };
    }
}
