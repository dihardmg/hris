package hris.hris.service;

import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfoService;
import org.flywaydb.core.api.output.MigrateResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class FlywayService {

    @Autowired
    private DataSource dataSource;

    public MigrateResult migrate() {
        log.info("Starting Flyway migration...");

        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration")
                .baselineOnMigrate(true)
                .validateOnMigrate(true)
                .outOfOrder(false)
                .load();

        try {
            MigrateResult result = flyway.migrate();
            log.info("Flyway migration completed successfully. {} migrations applied.",
                    result.migrationsExecuted);
            return result;
        } catch (Exception e) {
            log.error("Flyway migration failed", e);
            throw new RuntimeException("Migration failed: " + e.getMessage(), e);
        }
    }

    public MigrationInfoService info() {
        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration")
                .load();

        return flyway.info();
    }

    public boolean validate() {
        log.info("Validating Flyway migrations...");

        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration")
                .load();

        try {
            boolean result = flyway.validateWithResult().validationSuccessful;
            if (result) {
                log.info("Flyway validation passed");
            } else {
                log.warn("Flyway validation failed");
            }
            return result;
        } catch (Exception e) {
            log.error("Flyway validation error", e);
            return false;
        }
    }

    public void repair() {
        log.info("Running Flyway repair...");

        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration")
                .load();

        try {
            flyway.repair();
            log.info("Flyway repair completed successfully");
        } catch (Exception e) {
            log.error("Flyway repair failed", e);
            throw new RuntimeException("Repair failed: " + e.getMessage(), e);
        }
    }

    public Map<String, Object> getMigrationStatus() {
        MigrationInfoService info = info();
        Map<String, Object> status = new HashMap<>();

        status.put("currentVersion", info.current().getVersion() != null ?
                  info.current().getVersion().getVersion() : "No migrations applied");
        status.put("latestVersion", info.pending().length > 0 && info.pending()[0].getVersion() != null ?
                  info.pending()[0].getVersion().getVersion() : "No migrations available");
        status.put("pendingMigrations", info.pending().length);
        status.put("appliedMigrations", info.applied().length);

        boolean validationSuccessful = validate();
        status.put("validationSuccessful", validationSuccessful);

        return status;
    }

    public String[] getPendingMigrations() {
        MigrationInfoService info = info();
        String[] pending = new String[info.pending().length];

        for (int i = 0; i < info.pending().length; i++) {
            pending[i] = info.pending()[i].getVersion().getVersion() + " - " +
                         info.pending()[i].getDescription();
        }

        return pending;
    }

    public String[] getAppliedMigrations() {
        MigrationInfoService info = info();
        String[] applied = new String[info.applied().length];

        for (int i = 0; i < info.applied().length; i++) {
            applied[i] = info.applied()[i].getVersion().getVersion() + " - " +
                        info.applied()[i].getDescription() + " (applied on " +
                        info.applied()[i].getInstalledOn() + ")";
        }

        return applied;
    }

    public void clean() {
        log.warn("Running Flyway clean - this will drop all objects!");

        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration")
                .load();

        try {
            flyway.clean();
            log.info("Flyway clean completed successfully");
        } catch (Exception e) {
            log.error("Flyway clean failed", e);
            throw new RuntimeException("Clean failed: " + e.getMessage(), e);
        }
    }

    public void baseline() {
        log.info("Running Flyway baseline...");

        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration")
                .load();

        try {
            flyway.baseline();
            log.info("Flyway baseline completed successfully");
        } catch (Exception e) {
            log.error("Flyway baseline failed", e);
            throw new RuntimeException("Baseline failed: " + e.getMessage(), e);
        }
    }
}