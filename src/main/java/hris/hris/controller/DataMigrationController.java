package hris.hris.controller;

import hris.hris.service.DataMigrationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/migration")
@CrossOrigin(origins = "*", maxAge = 3600)
@Slf4j
public class DataMigrationController {

    @Autowired
    private DataMigrationService dataMigrationService;

    @PostMapping("/initialize")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> initializeDefaultData() {
        try {
            dataMigrationService.initializeDefaultData();

            return ResponseEntity.ok(Map.of(
                "message", "Default data initialization completed successfully"
            ));

        } catch (Exception e) {
            log.error("Initialize default data failed", e);
            return ResponseEntity.badRequest().body(
                Map.of("message", "Failed to initialize default data: " + e.getMessage())
            );
        }
    }

    @PostMapping("/import-employees")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> importEmployeesFromCSV(@RequestBody Map<String, String> requestBody) {
        try {
            String csvContent = requestBody.get("csvContent");

            if (csvContent == null || csvContent.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(
                    Map.of("message", "CSV content is required")
                );
            }

            dataMigrationService.migrateEmployeesFromCSV(csvContent);

            return ResponseEntity.ok(Map.of(
                "message", "Employee import completed successfully"
            ));

        } catch (Exception e) {
            log.error("Import employees failed", e);
            return ResponseEntity.badRequest().body(
                Map.of("message", "Failed to import employees: " + e.getMessage())
            );
        }
    }

    @PostMapping("/backup")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> backupEmployeeData() {
        try {
            dataMigrationService.backupEmployeeData();

            return ResponseEntity.ok(Map.of(
                "message", "Employee data backup completed successfully"
            ));

        } catch (Exception e) {
            log.error("Backup employee data failed", e);
            return ResponseEntity.badRequest().body(
                Map.of("message", "Failed to backup employee data: " + e.getMessage())
            );
        }
    }

    @GetMapping("/csv-template")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getCSVTemplate() {
        try {
            String template = "FirstName,LastName,Email,PhoneNumber,DepartmentId,PositionId,SupervisorId,HireDate,AnnualLeaveBalance,SickLeaveBalance\n" +
                             "John,Doe,john.doe@company.com,+1234567890,1,1,,2024-01-15,12,10\n" +
                             "Jane,Smith,jane.smith@company.com,+1234567891,1,2,1,2024-02-01,12,10";

            return ResponseEntity.ok(Map.of(
                "template", template,
                "message", "CSV template for employee import"
            ));

        } catch (Exception e) {
            log.error("Get CSV template failed", e);
            return ResponseEntity.badRequest().body(
                Map.of("message", "Failed to get CSV template")
            );
        }
    }
}