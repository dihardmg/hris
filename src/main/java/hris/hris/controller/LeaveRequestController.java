package hris.hris.controller;

import hris.hris.dto.ApiResponse;
import hris.hris.dto.LeaveRequestDto;
import hris.hris.dto.LeaveRequestResponseDto;
import hris.hris.dto.PaginatedLeaveRequestResponse;
import hris.hris.model.LeaveRequest;
import hris.hris.model.Employee;
import hris.hris.repository.EmployeeRepository;
import hris.hris.security.JwtUtil;
import hris.hris.service.LeaveRequestService;
import hris.hris.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/leave")
@CrossOrigin(origins = "*", maxAge = 3600)
@Slf4j
public class LeaveRequestController {

    @Autowired
    private LeaveRequestService leaveRequestService;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private JwtUtil jwtUtil;

    // Helper method to get employee ID from JWT token or Security Context
    private Long getEmployeeIdFromRequest(String token) {
        if (token != null && !token.isEmpty()) {
            return jwtUtil.getEmployeeIdFromToken(token.substring(7));
        } else {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            return employeeRepository.findByEmail(username)
                    .map(Employee::getId)
                    .orElseThrow(() -> new RuntimeException("Employee not found: " + username));
        }
    }

    @PostMapping("/request")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<ApiResponse<LeaveRequestResponseDto>> createLeaveRequest(
            @RequestHeader(value = "Authorization", required = false) String token,
            @Valid @RequestBody LeaveRequestDto requestDto) {
        try {
            Long employeeId = getEmployeeIdFromRequest(token);

            LeaveRequest leaveRequest = leaveRequestService.createLeaveRequest(employeeId, requestDto);

            // Get remaining balance after submission
            Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
            int remainingBalance = leaveRequestService.getAvailableLeaveBalance(employee, leaveRequest.getLeaveType());

            // Create optimized response DTO
            LeaveRequestResponseDto responseDto = LeaveRequestResponseDto.fromLeaveRequest(leaveRequest, remainingBalance);

            return ResponseEntity.status(201).body(ApiResponse.success(responseDto, "Leave request submitted successfully"));

        } catch (Exception e) {
            log.error("Create leave request failed", e);
            String errorMessage = e.getMessage() != null ? e.getMessage() : "Unknown error occurred";
            return ResponseEntity.badRequest().body(ApiResponse.error(errorMessage));
        }
    }

    @GetMapping("/my-requests")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<PaginatedLeaveRequestResponse> getMyLeaveRequests(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestParam(defaultValue = "30") int days,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Long employeeId = getEmployeeIdFromRequest(token);
            Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

            // Create pageable request
            Pageable pageable = PageRequest.of(page, size);
            Page<LeaveRequest> requestsPage = leaveRequestService.getEmployeeLeaveRequests(employeeId, pageable, days);

            // Convert to DTOs to avoid lazy loading issues
            List<LeaveRequestResponseDto> requestDtos = requestsPage.getContent().stream()
                .map(request -> {
                    int remainingBalance = leaveRequestService.getAvailableLeaveBalance(employee, request.getLeaveType());
                    return LeaveRequestResponseDto.fromLeaveRequest(request, remainingBalance);
                })
                .toList();

            // Create paginated response with real pagination data
            PaginatedLeaveRequestResponse response = PaginatedLeaveRequestResponse.createResponse(
                requestDtos,
                requestsPage.getSize(),         // size per page
                (int) requestsPage.getTotalElements(), // total elements
                requestsPage.getTotalPages(),    // total pages
                requestsPage.getNumber() + 1     // current page (convert from 0-based to 1-based)
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Get leave requests failed", e);
            // Print error message to console for debugging
            System.err.println("ERROR: " + e.getMessage());
            e.printStackTrace();
            // Return empty paginated response for error case
            PaginatedLeaveRequestResponse errorResponse = PaginatedLeaveRequestResponse.createResponse(
                List.of(),      // empty data
                0,              // size
                0,              // total
                0,              // totalPages
                0               // currentPage
            );
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @GetMapping("/request/{requestId}")
    @PreAuthorize("hasRole('EMPLOYEE') or hasRole('SUPERVISOR') or hasRole('HR') or hasRole('ADMIN')")
    public ResponseEntity<?> getLeaveRequestById(@RequestHeader(value = "Authorization", required = false) String token,
                                                @PathVariable Long requestId) {
        try {
            Long currentEmployeeId = getEmployeeIdFromRequest(token);

            var leaveRequestOptional = leaveRequestService.getLeaveRequestById(requestId);
            if (leaveRequestOptional.isEmpty()) {
                return ResponseEntity.status(403).body(ApiResponse.error("Access denied."));
            }

            LeaveRequest leaveRequest = leaveRequestOptional.get();
            Employee employee = leaveRequest.getEmployee();

            // Authorization check: Employees can only view their own requests,
            // Supervisors, HR, and Admin can view all requests
            if (!currentEmployeeId.equals(employee.getId())) {
                // Check if current user has supervisor, HR, or admin role
                Employee currentUser = employeeRepository.findById(currentEmployeeId)
                    .orElseThrow(() -> new RuntimeException("Current user not found"));

                String userEmail = currentUser.getEmail();
                boolean isSupervisorOrAbove = "supervisor@hris.com".equals(userEmail) ||
                                           "hr@hris.com".equals(userEmail) ||
                                           "admin@hris.com".equals(userEmail);

                if (!isSupervisorOrAbove) {
                    return ResponseEntity.status(403).body(ApiResponse.error("You are not authorized to view this leave request"));
                }
            }

            // Calculate remaining balance
            int remainingBalance = leaveRequestService.getAvailableLeaveBalance(employee, leaveRequest.getLeaveType());
            LeaveRequestResponseDto responseDto = LeaveRequestResponseDto.fromLeaveRequest(leaveRequest, remainingBalance);

            return ResponseEntity.ok(ApiResponse.success(responseDto, "Leave request retrieved successfully"));

        } catch (Exception e) {
            log.error("Get leave request by ID failed", e);
            String errorMessage = e.getMessage() != null ? e.getMessage() : "Failed to get leave request";
            return ResponseEntity.badRequest().body(ApiResponse.error(errorMessage));
        }
    }

    @GetMapping("/current")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<?> getCurrentLeave(@RequestHeader(value = "Authorization", required = false) String token) {
        try {
            Long employeeId = getEmployeeIdFromRequest(token);
            Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

            var currentLeave = leaveRequestService.getCurrentLeave(employeeId);

            if (currentLeave.isPresent()) {
                int remainingBalance = leaveRequestService.getAvailableLeaveBalance(employee, currentLeave.get().getLeaveType());
                LeaveRequestResponseDto currentLeaveDto = LeaveRequestResponseDto.fromLeaveRequest(currentLeave.get(), remainingBalance);
                return ResponseEntity.ok(ApiResponse.success(currentLeaveDto, "Current leave found"));
            } else {
                return ResponseEntity.ok(ApiResponse.success(null, "No current leave found"));
            }

        } catch (Exception e) {
            log.error("Get current leave failed", e);
            String errorMessage = e.getMessage() != null ? e.getMessage() : "Failed to get current leave";
            return ResponseEntity.badRequest().body(ApiResponse.error(errorMessage));
        }
    }

    @GetMapping("/supervisor/pending")
    @PreAuthorize("hasRole('SUPERVISOR')")
    public ResponseEntity<?> getPendingLeaveRequests(@RequestHeader(value = "Authorization", required = false) String token) {
        try {
            Long supervisorId = getEmployeeIdFromRequest(token);

            List<LeaveRequest> pendingRequests = leaveRequestService.getPendingRequestsForSupervisor(supervisorId);

            return ResponseEntity.ok(pendingRequests);

        } catch (Exception e) {
            log.error("Get pending leave requests failed", e);
            String errorMessage = e.getMessage() != null ? e.getMessage() : "Failed to get pending leave requests";
            return ResponseEntity.badRequest().body(
                Map.of("message", errorMessage)
            );
        }
    }

    @PostMapping("/supervisor/approve/{requestId}")
    @PreAuthorize("hasRole('SUPERVISOR')")
    public ResponseEntity<?> approveLeaveRequest(@RequestHeader(value = "Authorization", required = false) String token,
                                                @PathVariable Long requestId) {
        try {
            Long supervisorId = getEmployeeIdFromRequest(token);

            LeaveRequest approvedRequest = leaveRequestService.approveLeaveRequest(requestId, supervisorId);

            // Convert to DTO for consistent response
            Employee employee = approvedRequest.getEmployee();
            int remainingBalance = leaveRequestService.getAvailableLeaveBalance(employee, approvedRequest.getLeaveType());
            LeaveRequestResponseDto responseDto = LeaveRequestResponseDto.fromLeaveRequest(approvedRequest, remainingBalance);

            return ResponseEntity.ok(ApiResponse.success(responseDto, "Leave request approved successfully"));

        } catch (Exception e) {
            log.error("Approve leave request failed", e);
            String errorMessage = e.getMessage() != null ? e.getMessage() : "Unknown error occurred";
            return ResponseEntity.badRequest().body(ApiResponse.error(errorMessage));
        }
    }

    @PostMapping("/supervisor/reject/{requestId}")
    @PreAuthorize("hasRole('SUPERVISOR')")
    public ResponseEntity<?> rejectLeaveRequest(@RequestHeader(value = "Authorization", required = false) String token,
                                               @PathVariable Long requestId) {
        try {
            Long supervisorId = getEmployeeIdFromRequest(token);

            LeaveRequest rejectedRequest = leaveRequestService.rejectLeaveRequest(requestId, supervisorId);

            // Convert to DTO for consistent response
            Employee employee = rejectedRequest.getEmployee();
            int remainingBalance = leaveRequestService.getAvailableLeaveBalance(employee, rejectedRequest.getLeaveType());
            LeaveRequestResponseDto responseDto = LeaveRequestResponseDto.fromLeaveRequest(rejectedRequest, remainingBalance);

            return ResponseEntity.ok(ApiResponse.success(responseDto, "Leave request rejected successfully"));

        } catch (Exception e) {
            log.error("Reject leave request failed", e);
            String errorMessage = e.getMessage() != null ? e.getMessage() : "Unknown error occurred";
            return ResponseEntity.badRequest().body(ApiResponse.error(errorMessage));
        }
    }

    @GetMapping("/balance")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<?> getLeaveBalance(@RequestHeader(value = "Authorization", required = false) String token) {
        try {
            return ResponseEntity.ok(Map.of("message", "Leave balance endpoint - implement as needed"));
        } catch (Exception e) {
            log.error("Get leave balance failed", e);
            String errorMessage = e.getMessage() != null ? e.getMessage() : "Failed to get leave balance";
            return ResponseEntity.badRequest().body(
                Map.of("message", errorMessage)
            );
        }
    }
}