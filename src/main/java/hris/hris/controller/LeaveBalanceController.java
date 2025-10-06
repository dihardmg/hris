package hris.hris.controller;

import hris.hris.dto.LeaveBalanceDto;
import hris.hris.model.Employee;
import hris.hris.repository.EmployeeRepository;
import hris.hris.service.LeaveBalanceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/leave-balance")
@CrossOrigin(origins = "*")
@Slf4j
public class LeaveBalanceController {

    @Autowired
    private LeaveBalanceService leaveBalanceService;

    @Autowired
    private EmployeeRepository employeeRepository;

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<LeaveBalanceDto>> getEmployeeLeaveBalances(@PathVariable Long employeeId) {
        List<LeaveBalanceDto> balances = leaveBalanceService.getAllLeaveBalancesForEmployee(employeeId);
        return ResponseEntity.ok(balances);
    }

    @GetMapping("/employee/{employeeId}/leave-type/{leaveTypeId}")
    public ResponseEntity<LeaveBalanceDto> getEmployeeLeaveBalanceForType(
            @PathVariable Long employeeId,
            @PathVariable Long leaveTypeId) {
        return leaveBalanceService.getLeaveBalanceForEmployeeAndType(employeeId, leaveTypeId)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/my-balances")
    public ResponseEntity<List<LeaveBalanceDto>> getMyLeaveBalances() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        Employee employee = employeeRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Employee not found"));

        List<LeaveBalanceDto> balances = leaveBalanceService.getAllLeaveBalancesForEmployee(employee.getId());
        return ResponseEntity.ok(balances);
    }

    @GetMapping("/my-balances/leave-type/{leaveTypeId}")
    public ResponseEntity<LeaveBalanceDto> getMyLeaveBalanceForType(@PathVariable Long leaveTypeId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        Employee employee = employeeRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Employee not found"));

        return leaveBalanceService.getLeaveBalanceForEmployeeAndType(employee.getId(), leaveTypeId)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/subordinates")
    public ResponseEntity<List<LeaveBalanceDto>> getSubordinateLeaveBalances() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        Employee supervisor = employeeRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Supervisor not found"));

        List<LeaveBalanceDto> balances = leaveBalanceService.getLeaveBalancesForSupervisor(supervisor.getId());
        return ResponseEntity.ok(balances);
    }

    @GetMapping("/subordinate/{employeeId}")
    public ResponseEntity<List<LeaveBalanceDto>> getSubordinateLeaveBalances(@PathVariable Long employeeId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        Employee supervisor = employeeRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Supervisor not found"));

        // Validate that the requested employee is actually a subordinate
        Optional<Employee> subordinate = employeeRepository.findById(employeeId);
        if (subordinate.isEmpty() || !subordinate.get().getSupervisorId().equals(supervisor.getId())) {
            return ResponseEntity.status(403).build();
        }

        List<LeaveBalanceDto> balances = leaveBalanceService.getAllLeaveBalancesForEmployee(employeeId);
        return ResponseEntity.ok(balances);
    }

    @GetMapping("/all")
    public ResponseEntity<List<LeaveBalanceDto>> getAllLeaveBalances() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        Employee manager = employeeRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Manager not found"));

        // Only allow managers or HR to view all balances
        if (!isManagerOrHR(manager)) {
            return ResponseEntity.status(403).build();
        }

        List<LeaveBalanceDto> balances = leaveBalanceService.getLeaveBalancesForManager();
        return ResponseEntity.ok(balances);
    }

    private boolean isManagerOrHR(Employee employee) {
        // Implement logic to check if employee is manager or HR
        // This could be based on position, department, or a role field
        return employee.getSupervisorId() == null || // Top-level manager
               employee.getDepartmentId() != null && employee.getDepartmentId() == 1; // HR department
    }
}