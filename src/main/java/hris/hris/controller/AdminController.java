package hris.hris.controller;

import hris.hris.dto.EmployeeRegistrationDto;
import hris.hris.model.Employee;
import hris.hris.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*", maxAge = 3600)
@Slf4j
public class AdminController {

    @Autowired
    private EmployeeService employeeService;

    @PostMapping("/register-employee")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public ResponseEntity<?> registerEmployee(@Valid @RequestBody EmployeeRegistrationDto registrationDto) {
        try {
            Employee employee = employeeService.registerEmployee(registrationDto);

            return ResponseEntity.ok(Map.of(
                "message", "Employee registered successfully",
                "employee", employee
            ));

        } catch (Exception e) {
            log.error("Employee registration failed", e);
            return ResponseEntity.badRequest().body(
                Map.of("message", e.getMessage())
            );
        }
    }

    @GetMapping("/employees")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public ResponseEntity<?> getAllEmployees() {
        try {
            List<Employee> employees = employeeService.getAllActiveEmployees();
            return ResponseEntity.ok(employees);
        } catch (Exception e) {
            log.error("Get all employees failed", e);
            return ResponseEntity.badRequest().body(
                Map.of("message", "Failed to get employees")
            );
        }
    }

    @GetMapping("/employees/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public ResponseEntity<?> getEmployeeById(@PathVariable Long id) {
        try {
            Optional<Employee> employee = employeeService.getEmployeeById(id);

            if (employee.isPresent()) {
                return ResponseEntity.ok(employee.get());
            } else {
                return ResponseEntity.notFound().build();
            }

        } catch (Exception e) {
            log.error("Get employee by ID failed", e);
            return ResponseEntity.badRequest().body(
                Map.of("message", "Failed to get employee")
            );
        }
    }

    @GetMapping("/employees/by-employee-id/{employeeId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public ResponseEntity<?> getEmployeeByEmployeeId(@PathVariable String employeeId) {
        try {
            Optional<Employee> employee = employeeService.getEmployeeByEmployeeId(employeeId);

            if (employee.isPresent()) {
                return ResponseEntity.ok(employee.get());
            } else {
                return ResponseEntity.notFound().build();
            }

        } catch (Exception e) {
            log.error("Get employee by employee ID failed", e);
            return ResponseEntity.badRequest().body(
                Map.of("message", "Failed to get employee")
            );
        }
    }

    @PutMapping("/employees/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public ResponseEntity<?> updateEmployee(@PathVariable Long id,
                                           @Valid @RequestBody EmployeeRegistrationDto updateDto) {
        try {
            Employee employee = employeeService.updateEmployee(id, updateDto);

            return ResponseEntity.ok(Map.of(
                "message", "Employee updated successfully",
                "employee", employee
            ));

        } catch (Exception e) {
            log.error("Update employee failed", e);
            return ResponseEntity.badRequest().body(
                Map.of("message", e.getMessage())
            );
        }
    }

    @PostMapping("/employees/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public ResponseEntity<?> deactivateEmployee(@PathVariable Long id) {
        try {
            employeeService.deactivateEmployee(id);

            return ResponseEntity.ok(Map.of(
                "message", "Employee deactivated successfully"
            ));

        } catch (Exception e) {
            log.error("Deactivate employee failed", e);
            return ResponseEntity.badRequest().body(
                Map.of("message", e.getMessage())
            );
        }
    }

    @PostMapping("/employees/{id}/activate")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public ResponseEntity<?> activateEmployee(@PathVariable Long id) {
        try {
            employeeService.activateEmployee(id);

            return ResponseEntity.ok(Map.of(
                "message", "Employee activated successfully"
            ));

        } catch (Exception e) {
            log.error("Activate employee failed", e);
            return ResponseEntity.badRequest().body(
                Map.of("message", e.getMessage())
            );
        }
    }

    @PostMapping("/employees/{id}/face-template")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public ResponseEntity<?> updateFaceTemplate(@PathVariable Long id,
                                                @RequestBody Map<String, String> requestBody) {
        try {
            String faceImage = requestBody.get("faceImage");

            if (faceImage == null || faceImage.isEmpty()) {
                return ResponseEntity.badRequest().body(
                    Map.of("message", "Face image is required")
                );
            }

            Employee employee = employeeService.updateFaceTemplate(id, faceImage);

            return ResponseEntity.ok(Map.of(
                "message", "Face template updated successfully",
                "employee", employee
            ));

        } catch (Exception e) {
            log.error("Update face template failed", e);
            return ResponseEntity.badRequest().body(
                Map.of("message", e.getMessage())
            );
        }
    }

    @GetMapping("/supervisors/{supervisorId}/subordinates")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public ResponseEntity<?> getSubordinates(@PathVariable Long supervisorId) {
        try {
            List<Employee> subordinates = employeeService.getSubordinates(supervisorId);
            return ResponseEntity.ok(subordinates);
        } catch (Exception e) {
            log.error("Get subordinates failed", e);
            return ResponseEntity.badRequest().body(
                Map.of("message", "Failed to get subordinates")
            );
        }
    }

    @GetMapping("/reports/attendance")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public ResponseEntity<?> getAttendanceReport(@RequestParam(required = false) String startDate,
                                                 @RequestParam(required = false) String endDate,
                                                 @RequestParam(required = false) Long departmentId) {
        try {
            return ResponseEntity.ok(Map.of(
                "message", "Attendance report endpoint - implement as needed",
                "startDate", startDate,
                "endDate", endDate,
                "departmentId", departmentId
            ));
        } catch (Exception e) {
            log.error("Get attendance report failed", e);
            return ResponseEntity.badRequest().body(
                Map.of("message", "Failed to generate attendance report")
            );
        }
    }

    @GetMapping("/reports/leave")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public ResponseEntity<?> getLeaveReport(@RequestParam(required = false) String startDate,
                                           @RequestParam(required = false) String endDate,
                                           @RequestParam(required = false) Long departmentId) {
        try {
            return ResponseEntity.ok(Map.of(
                "message", "Leave report endpoint - implement as needed",
                "startDate", startDate,
                "endDate", endDate,
                "departmentId", departmentId
            ));
        } catch (Exception e) {
            log.error("Get leave report failed", e);
            return ResponseEntity.badRequest().body(
                Map.of("message", "Failed to generate leave report")
            );
        }
    }

    @GetMapping("/reports/business-travel")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public ResponseEntity<?> getBusinessTravelReport(@RequestParam(required = false) String startDate,
                                                     @RequestParam(required = false) String endDate,
                                                     @RequestParam(required = false) Long departmentId) {
        try {
            return ResponseEntity.ok(Map.of(
                "message", "Business travel report endpoint - implement as needed",
                "startDate", startDate,
                "endDate", endDate,
                "departmentId", departmentId
            ));
        } catch (Exception e) {
            log.error("Get business travel report failed", e);
            return ResponseEntity.badRequest().body(
                Map.of("message", "Failed to generate business travel report")
            );
        }
    }
}