package hris.hris.controller;

import hris.hris.dto.LeaveRequestDto;
import hris.hris.model.LeaveRequest;
import hris.hris.security.JwtUtil;
import hris.hris.service.LeaveRequestService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
    private JwtUtil jwtUtil;

    @PostMapping("/request")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<?> createLeaveRequest(@RequestHeader("Authorization") String token,
                                              @Valid @RequestBody LeaveRequestDto requestDto) {
        try {
            Long employeeId = jwtUtil.getEmployeeIdFromToken(token.substring(7));

            LeaveRequest leaveRequest = leaveRequestService.createLeaveRequest(employeeId, requestDto);

            return ResponseEntity.ok(Map.of(
                "message", "Leave request submitted successfully",
                "leaveRequest", leaveRequest
            ));

        } catch (Exception e) {
            log.error("Create leave request failed", e);
            return ResponseEntity.badRequest().body(
                Map.of("message", e.getMessage())
            );
        }
    }

    @GetMapping("/my-requests")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<?> getMyLeaveRequests(@RequestHeader("Authorization") String token) {
        try {
            Long employeeId = jwtUtil.getEmployeeIdFromToken(token.substring(7));

            List<LeaveRequest> requests = leaveRequestService.getEmployeeLeaveRequests(employeeId);

            return ResponseEntity.ok(requests);

        } catch (Exception e) {
            log.error("Get leave requests failed", e);
            return ResponseEntity.badRequest().body(
                Map.of("message", "Failed to get leave requests")
            );
        }
    }

    @GetMapping("/current")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<?> getCurrentLeave(@RequestHeader("Authorization") String token) {
        try {
            Long employeeId = jwtUtil.getEmployeeIdFromToken(token.substring(7));

            var currentLeave = leaveRequestService.getCurrentLeave(employeeId);

            if (currentLeave.isPresent()) {
                return ResponseEntity.ok(currentLeave.get());
            } else {
                return ResponseEntity.ok(Map.of("message", "No current leave found"));
            }

        } catch (Exception e) {
            log.error("Get current leave failed", e);
            return ResponseEntity.badRequest().body(
                Map.of("message", "Failed to get current leave")
            );
        }
    }

    @GetMapping("/supervisor/pending")
    @PreAuthorize("hasRole('SUPERVISOR')")
    public ResponseEntity<?> getPendingLeaveRequests(@RequestHeader("Authorization") String token) {
        try {
            Long supervisorId = jwtUtil.getEmployeeIdFromToken(token.substring(7));

            List<LeaveRequest> pendingRequests = leaveRequestService.getPendingRequestsForSupervisor(supervisorId);

            return ResponseEntity.ok(pendingRequests);

        } catch (Exception e) {
            log.error("Get pending leave requests failed", e);
            return ResponseEntity.badRequest().body(
                Map.of("message", "Failed to get pending leave requests")
            );
        }
    }

    @PostMapping("/supervisor/approve/{requestId}")
    @PreAuthorize("hasRole('SUPERVISOR')")
    public ResponseEntity<?> approveLeaveRequest(@RequestHeader("Authorization") String token,
                                                @PathVariable Long requestId,
                                                @RequestBody Map<String, String> requestBody) {
        try {
            Long supervisorId = jwtUtil.getEmployeeIdFromToken(token.substring(7));
            String notes = requestBody.getOrDefault("notes", "");

            LeaveRequest approvedRequest = leaveRequestService.approveLeaveRequest(requestId, supervisorId, notes);

            return ResponseEntity.ok(Map.of(
                "message", "Leave request approved successfully",
                "leaveRequest", approvedRequest
            ));

        } catch (Exception e) {
            log.error("Approve leave request failed", e);
            return ResponseEntity.badRequest().body(
                Map.of("message", e.getMessage())
            );
        }
    }

    @PostMapping("/supervisor/reject/{requestId}")
    @PreAuthorize("hasRole('SUPERVISOR')")
    public ResponseEntity<?> rejectLeaveRequest(@RequestHeader("Authorization") String token,
                                               @PathVariable Long requestId,
                                               @RequestBody Map<String, String> requestBody) {
        try {
            Long supervisorId = jwtUtil.getEmployeeIdFromToken(token.substring(7));
            String rejectionReason = requestBody.get("rejectionReason");

            if (rejectionReason == null || rejectionReason.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(
                    Map.of("message", "Rejection reason is required")
                );
            }

            LeaveRequest rejectedRequest = leaveRequestService.rejectLeaveRequest(requestId, supervisorId, rejectionReason);

            return ResponseEntity.ok(Map.of(
                "message", "Leave request rejected successfully",
                "leaveRequest", rejectedRequest
            ));

        } catch (Exception e) {
            log.error("Reject leave request failed", e);
            return ResponseEntity.badRequest().body(
                Map.of("message", e.getMessage())
            );
        }
    }

    @GetMapping("/balance")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<?> getLeaveBalance(@RequestHeader("Authorization") String token) {
        try {
            return ResponseEntity.ok(Map.of("message", "Leave balance endpoint - implement as needed"));
        } catch (Exception e) {
            log.error("Get leave balance failed", e);
            return ResponseEntity.badRequest().body(
                Map.of("message", "Failed to get leave balance")
            );
        }
    }
}