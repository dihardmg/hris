package hris.hris.controller;

import hris.hris.model.Employee;
import hris.hris.model.HrQuota;
import hris.hris.repository.EmployeeRepository;
import hris.hris.repository.HrQuotaRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.Year;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/hr-quota")
@CrossOrigin(origins = "*")
@Slf4j
public class HrQuotaController {

    @Autowired
    private HrQuotaRepository hrQuotaRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @GetMapping("/my-quota")
    public ResponseEntity<Map<String, Object>> getMyHrQuota(@RequestParam(required = false) Integer tahun) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        Employee employee = employeeRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Employee not found"));

        Year targetYear = (tahun != null) ? Year.of(tahun) : Year.now();

        Optional<HrQuota> quotaOpt = hrQuotaRepository.findByIdEmployeeAndTahun(employee.getId(), targetYear);

        if (quotaOpt.isEmpty()) {
            // Return empty quota if not found
            Map<String, Object> response = new HashMap<>();
            response.put("id", null);
            response.put("idEmployee", employee.getId());
            response.put("tahun", targetYear.getValue());
            response.put("cutiTahunan", 0);
            response.put("cutiTahunanTerpakai", 0);
            response.put("sisaCutiTahunan", 0);
            response.put("message", "No quota found for year " + targetYear.getValue());

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("data", response);
            return ResponseEntity.ok(result);
        }

        HrQuota quota = quotaOpt.get();

        Map<String, Object> response = new HashMap<>();
        response.put("id", quota.getId());
        response.put("idEmployee", quota.getIdEmployee());
        response.put("tahun", quota.getTahun().getValue());
        response.put("cutiTahunan", quota.getCutiTahunan());
        response.put("cutiTahunanTerpakai", quota.getCutiTahunanTerpakai());
        response.put("sisaCutiTahunan", quota.getSisaCutiTahunan());

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", response);

        return ResponseEntity.ok(result);
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<Map<String, Object>> getHrQuotaByEmployeeId(
            @PathVariable Long employeeId,
            @RequestParam(required = false) Integer tahun) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        Employee supervisor = employeeRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Supervisor not found"));

        // Validate that the requested employee is a subordinate
        Optional<Employee> subordinate = employeeRepository.findById(employeeId);
        if (subordinate.isEmpty() || !subordinate.get().getSupervisorId().equals(supervisor.getId())) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Access denied. You can only view quotas of your subordinates.");
            return ResponseEntity.status(403).body(error);
        }

        Year targetYear = (tahun != null) ? Year.of(tahun) : Year.now();

        Optional<HrQuota> quotaOpt = hrQuotaRepository.findByIdEmployeeAndTahun(employeeId, targetYear);

        if (quotaOpt.isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "No quota found for employee " + employeeId + " in year " + targetYear.getValue());
            return ResponseEntity.notFound().build();
        }

        HrQuota quota = quotaOpt.get();

        Map<String, Object> response = new HashMap<>();
        response.put("id", quota.getId());
        response.put("idEmployee", quota.getIdEmployee());
        response.put("tahun", quota.getTahun().getValue());
        response.put("cutiTahunan", quota.getCutiTahunan());
        response.put("cutiTahunanTerpakai", quota.getCutiTahunanTerpakai());
        response.put("sisaCutiTahunan", quota.getSisaCutiTahunan());

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", response);

        return ResponseEntity.ok(result);
    }

    @GetMapping("/all")
    public ResponseEntity<Map<String, Object>> getAllHrQuotasByYear(@RequestParam(required = false) Integer tahun) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        Employee manager = employeeRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Manager not found"));

        // Only allow managers or HR to view all quotas
        if (!isManagerOrHR(manager)) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Access denied. Manager or HR role required.");
            return ResponseEntity.status(403).body(error);
        }

        Year targetYear = (tahun != null) ? Year.of(tahun) : Year.now();

        List<HrQuota> quotas = hrQuotaRepository.findByTahun(targetYear);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", quotas);

        return ResponseEntity.ok(result);
    }

    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createInitialHrQuota(@RequestBody Map<String, Object> request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        Employee manager = employeeRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Manager not found"));

        // Only allow managers or HR to create quotas
        if (!isManagerOrHR(manager)) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Access denied. Manager or HR role required.");
            return ResponseEntity.status(403).body(error);
        }

        Long employeeId = ((Number) request.get("employeeId")).longValue();
        Integer tahunValue = (Integer) request.get("tahun");
        Integer initialQuota = (Integer) request.get("initialQuota");

        // Validate employee exists
        Optional<Employee> employee = employeeRepository.findById(employeeId);
        if (employee.isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Employee not found");
            return ResponseEntity.notFound().build();
        }

        Year targetYear = Year.of(tahunValue);

        // Check if quota already exists
        if (hrQuotaRepository.existsByIdEmployeeAndTahun(employeeId, targetYear)) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Quota already exists for employee " + employeeId + " in year " + tahunValue);
            return ResponseEntity.badRequest().body(error);
        }

        // Create new quota
        HrQuota quota = new HrQuota();
        quota.setIdEmployee(employeeId);
        quota.setTahun(targetYear);
        quota.setCutiTahunan(initialQuota);
        quota.setCutiTahunanTerpakai(0);

        HrQuota savedQuota = hrQuotaRepository.save(quota);

        Map<String, Object> response = new HashMap<>();
        response.put("id", savedQuota.getId());
        response.put("idEmployee", savedQuota.getIdEmployee());
        response.put("tahun", savedQuota.getTahun().getValue());
        response.put("cutiTahunan", savedQuota.getCutiTahunan());
        response.put("cutiTahunanTerpakai", savedQuota.getCutiTahunanTerpakai());
        response.put("sisaCutiTahunan", savedQuota.getSisaCutiTahunan());

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", response);

        return ResponseEntity.status(201).body(result);
    }

    @PutMapping("/update/{employeeId}")
    public ResponseEntity<Map<String, Object>> updateHrQuota(
            @PathVariable Long employeeId,
            @RequestBody Map<String, Object> request,
            @RequestParam(required = false) Integer tahun) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        Employee manager = employeeRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Manager not found"));

        // Only allow managers or HR to update quotas
        if (!isManagerOrHR(manager)) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Access denied. Manager or HR role required.");
            return ResponseEntity.status(403).body(error);
        }

        Year targetYear = (tahun != null) ? Year.of(tahun) : Year.now();
        Integer newQuota = (Integer) request.get("newQuota");

        Optional<HrQuota> quotaOpt = hrQuotaRepository.findByIdEmployeeAndTahun(employeeId, targetYear);
        if (quotaOpt.isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "No quota found for employee " + employeeId + " in year " + targetYear.getValue());
            return ResponseEntity.notFound().build();
        }

        HrQuota quota = quotaOpt.get();
        quota.setCutiTahunan(newQuota);

        HrQuota savedQuota = hrQuotaRepository.save(quota);

        Map<String, Object> response = new HashMap<>();
        response.put("id", savedQuota.getId());
        response.put("idEmployee", savedQuota.getIdEmployee());
        response.put("tahun", savedQuota.getTahun().getValue());
        response.put("cutiTahunan", savedQuota.getCutiTahunan());
        response.put("cutiTahunanTerpakai", savedQuota.getCutiTahunanTerpakai());
        response.put("sisaCutiTahunan", savedQuota.getSisaCutiTahunan());

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", response);

        return ResponseEntity.ok(result);
    }

    private boolean isManagerOrHR(Employee employee) {
        // Implement logic to check if employee is manager or HR
        // This could be based on position, department, or a role field
        return employee.getSupervisorId() == null || // Top-level manager
               employee.getDepartmentId() != null && employee.getDepartmentId() == 1; // HR department
    }
}