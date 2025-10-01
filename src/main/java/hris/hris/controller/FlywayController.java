package hris.hris.controller;

import hris.hris.service.FlywayService;
import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.api.output.MigrateResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/flyway")
@CrossOrigin(origins = "*", maxAge = 3600)
@Slf4j
public class FlywayController {

    @Autowired
    private FlywayService flywayService;

    @PostMapping("/migrate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> migrate() {
        try {
            MigrateResult result = flywayService.migrate();

            return ResponseEntity.ok(Map.of(
                "message", "Migration completed successfully",
                "migrationsExecuted", result.migrationsExecuted,
                "flywayVersion", result.flywayVersion
            ));

        } catch (Exception e) {
            log.error("Migration failed", e);
            return ResponseEntity.internalServerError().body(
                Map.of("message", "Migration failed: " + e.getMessage())
            );
        }
    }

    @GetMapping("/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getStatus() {
        try {
            Map<String, Object> status = flywayService.getMigrationStatus();

            return ResponseEntity.ok(status);

        } catch (Exception e) {
            log.error("Get status failed", e);
            return ResponseEntity.internalServerError().body(
                Map.of("message", "Failed to get migration status: " + e.getMessage())
            );
        }
    }

    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getPendingMigrations() {
        try {
            String[] pending = flywayService.getPendingMigrations();

            return ResponseEntity.ok(Map.of(
                "pendingMigrations", pending,
                "count", pending.length
            ));

        } catch (Exception e) {
            log.error("Get pending migrations failed", e);
            return ResponseEntity.internalServerError().body(
                Map.of("message", "Failed to get pending migrations: " + e.getMessage())
            );
        }
    }

    @GetMapping("/applied")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAppliedMigrations() {
        try {
            String[] applied = flywayService.getAppliedMigrations();

            return ResponseEntity.ok(Map.of(
                "appliedMigrations", applied,
                "count", applied.length
            ));

        } catch (Exception e) {
            log.error("Get applied migrations failed", e);
            return ResponseEntity.internalServerError().body(
                Map.of("message", "Failed to get applied migrations: " + e.getMessage())
            );
        }
    }

    @PostMapping("/validate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> validate() {
        try {
            boolean isValid = flywayService.validate();

            return ResponseEntity.ok(Map.of(
                "valid", isValid,
                "message", isValid ? "Validation passed" : "Validation failed"
            ));

        } catch (Exception e) {
            log.error("Validate failed", e);
            return ResponseEntity.internalServerError().body(
                Map.of("message", "Validation failed: " + e.getMessage())
            );
        }
    }

    @PostMapping("/repair")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> repair() {
        try {
            flywayService.repair();

            return ResponseEntity.ok(Map.of(
                "message", "Repair completed successfully"
            ));

        } catch (Exception e) {
            log.error("Repair failed", e);
            return ResponseEntity.internalServerError().body(
                Map.of("message", "Repair failed: " + e.getMessage())
            );
        }
    }

    @PostMapping("/baseline")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> baseline() {
        try {
            flywayService.baseline();

            return ResponseEntity.ok(Map.of(
                "message", "Baseline completed successfully"
            ));

        } catch (Exception e) {
            log.error("Baseline failed", e);
            return ResponseEntity.internalServerError().body(
                Map.of("message", "Baseline failed: " + e.getMessage())
            );
        }
    }

    @PostMapping("/clean")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> clean() {
        try {
            flywayService.clean();

            return ResponseEntity.ok(Map.of(
                "message", "Clean completed successfully"
            ));

        } catch (Exception e) {
            log.error("Clean failed", e);
            return ResponseEntity.internalServerError().body(
                Map.of("message", "Clean failed: " + e.getMessage())
            );
        }
    }
}