package hris.hris.service;

import hris.hris.model.Employee;
import hris.hris.repository.EmployeeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class DataMigrationService {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private FaceRecognitionService faceRecognitionService;

    @Transactional
    public void migrateEmployeesFromCSV(String csvContent) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                new java.io.ByteArrayInputStream(csvContent.getBytes(StandardCharsets.UTF_8))))) {

            String headerLine = reader.readLine();
            String line;
            int successCount = 0;
            int errorCount = 0;
            List<String> errors = new ArrayList<>();

            while ((line = reader.readLine()) != null) {
                try {
                    String[] fields = line.split(",");
                    if (fields.length >= 6) {
                        Employee employee = parseEmployeeFromCSV(fields);

                        if (!employeeRepository.existsByEmail(employee.getEmail())) {
                            employeeRepository.save(employee);
                            successCount++;
                            log.info("Migrated employee: {}", employee.getEmail());
                        } else {
                            errors.add("Employee already exists: " + employee.getEmail());
                            errorCount++;
                        }
                    }
                } catch (Exception e) {
                    errors.add("Error processing line: " + line + " - " + e.getMessage());
                    errorCount++;
                }
            }

            log.info("Migration completed. Success: {}, Errors: {}", successCount, errorCount);
            if (!errors.isEmpty()) {
                errors.forEach(log::warn);
            }

        } catch (Exception e) {
            log.error("Failed to process CSV migration", e);
            throw new RuntimeException("CSV migration failed: " + e.getMessage());
        }
    }

    private Employee parseEmployeeFromCSV(String[] fields) {
        Employee employee = new Employee();

        employee.setFirstName(fields[0].trim());
        employee.setLastName(fields[1].trim());
        employee.setEmail(fields[2].trim());
        employee.setPassword(passwordEncoder.encode("defaultPassword123"));
        employee.setPhoneNumber(fields[3].trim());

        try {
            employee.setDepartmentId(Long.parseLong(fields[4].trim()));
        } catch (NumberFormatException e) {
            employee.setDepartmentId(1L);
        }

        try {
            employee.setPositionId(Long.parseLong(fields[5].trim()));
        } catch (NumberFormatException e) {
            employee.setPositionId(1L);
        }

        if (fields.length > 6 && !fields[6].trim().isEmpty()) {
            try {
                employee.setSupervisorId(Long.parseLong(fields[6].trim()));
            } catch (NumberFormatException e) {
                // ignore
            }
        }

        if (fields.length > 7 && !fields[7].trim().isEmpty()) {
            try {
                LocalDate hireDate = LocalDate.parse(fields[7].trim());
                employee.setHireDate(Date.from(hireDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));
            } catch (Exception e) {
                employee.setHireDate(new Date());
            }
        } else {
            employee.setHireDate(new Date());
        }

        employee.setIsActive(true);

        return employee;
    }

    @Transactional
    public void initializeDefaultData() {
        log.info("Initializing default data...");

        // Check if admin user already exists
        if (!employeeRepository.existsByEmail("admin@hris.com")) {
            createDefaultAdmin();
            log.info("Default admin user created");
        } else {
            log.info("Default admin user already exists");
        }
    }

    private void createDefaultAdmin() {
        Employee admin = new Employee();
        admin.setFirstName("System");
        admin.setLastName("Administrator");
        admin.setEmail("admin@hris.com");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setPhoneNumber("0000000000");
        admin.setDepartmentId(1L);
        admin.setPositionId(1L);
        admin.setHireDate(new Date());
        admin.setIsActive(true);

        employeeRepository.save(admin);
        log.info("Default admin user created: admin@hris.com / admin123");
    }

    @Transactional
    public void backupEmployeeData() {
        try {
            List<Employee> employees = employeeRepository.findByIsActiveTrue();

            StringBuilder csvContent = new StringBuilder();
            csvContent.append("FirstName,LastName,Email,PhoneNumber,DepartmentId,PositionId,SupervisorId,HireDate\n");

            for (Employee emp : employees) {
                csvContent.append(String.format("%s,%s,%s,%s,%d,%d,%s,%s\n",
                    escapeCsv(emp.getFirstName()),
                    escapeCsv(emp.getLastName()),
                    escapeCsv(emp.getEmail()),
                    escapeCsv(emp.getPhoneNumber()),
                    emp.getDepartmentId(),
                    emp.getPositionId(),
                    emp.getSupervisorId() != null ? emp.getSupervisorId() : "",
                    emp.getHireDate()
                ));
            }

            log.info("Employee data backup completed. {} employees exported.", employees.size());
            // In a real implementation, you would save this to a file or return it
            // For now, we just log the completion

        } catch (Exception e) {
            log.error("Failed to backup employee data", e);
            throw new RuntimeException("Backup failed: " + e.getMessage());
        }
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}