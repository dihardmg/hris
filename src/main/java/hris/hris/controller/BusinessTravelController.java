package hris.hris.controller;

import hris.hris.dto.BusinessTravelRequestDto;
import hris.hris.model.BusinessTravelRequest;
import hris.hris.security.JwtUtil;
import hris.hris.service.BusinessTravelRequestService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/business-travel")
@CrossOrigin(origins = "*", maxAge = 3600)
@Slf4j
public class BusinessTravelController {

    @Autowired
    private BusinessTravelRequestService businessTravelRequestService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/request")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<?> createBusinessTravelRequest(@RequestHeader("Authorization") String token,
                                                        @Valid @RequestBody BusinessTravelRequestDto requestDto) {
        try {
            Long employeeId = jwtUtil.getEmployeeIdFromToken(token.substring(7));

            BusinessTravelRequest travelRequest = businessTravelRequestService.createBusinessTravelRequest(employeeId, requestDto);

            return ResponseEntity.ok(Map.of(
                "message", "Business travel request submitted successfully",
                "travelRequest", travelRequest
            ));

        } catch (Exception e) {
            log.error("Create business travel request failed", e);
            return ResponseEntity.badRequest().body(
                Map.of("message", e.getMessage())
            );
        }
    }

    @GetMapping("/my-requests")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<?> getMyBusinessTravelRequests(@RequestHeader("Authorization") String token) {
        try {
            Long employeeId = jwtUtil.getEmployeeIdFromToken(token.substring(7));

            List<BusinessTravelRequest> requests = businessTravelRequestService.getEmployeeBusinessTravelRequests(employeeId);

            return ResponseEntity.ok(requests);

        } catch (Exception e) {
            log.error("Get business travel requests failed", e);
            return ResponseEntity.badRequest().body(
                Map.of("message", "Failed to get business travel requests")
            );
        }
    }

    @GetMapping("/current")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<?> getCurrentTravel(@RequestHeader("Authorization") String token) {
        try {
            Long employeeId = jwtUtil.getEmployeeIdFromToken(token.substring(7));

            List<BusinessTravelRequest> currentTravel = businessTravelRequestService.getCurrentTravel(employeeId);

            if (!currentTravel.isEmpty()) {
                return ResponseEntity.ok(currentTravel);
            } else {
                return ResponseEntity.ok(Map.of("message", "No current business travel found"));
            }

        } catch (Exception e) {
            log.error("Get current business travel failed", e);
            return ResponseEntity.badRequest().body(
                Map.of("message", "Failed to get current business travel")
            );
        }
    }

    @GetMapping("/supervisor/pending")
    @PreAuthorize("hasRole('SUPERVISOR')")
    public ResponseEntity<?> getPendingBusinessTravelRequests(@RequestHeader("Authorization") String token) {
        try {
            Long supervisorId = jwtUtil.getEmployeeIdFromToken(token.substring(7));

            List<BusinessTravelRequest> pendingRequests = businessTravelRequestService.getPendingRequestsForSupervisor(supervisorId);

            return ResponseEntity.ok(pendingRequests);

        } catch (Exception e) {
            log.error("Get pending business travel requests failed", e);
            return ResponseEntity.badRequest().body(
                Map.of("message", "Failed to get pending business travel requests")
            );
        }
    }

    @PostMapping("/supervisor/approve/{requestId}")
    @PreAuthorize("hasRole('SUPERVISOR')")
    public ResponseEntity<?> approveBusinessTravelRequest(@RequestHeader("Authorization") String token,
                                                        @PathVariable Long requestId,
                                                        @RequestBody Map<String, String> requestBody) {
        try {
            Long supervisorId = jwtUtil.getEmployeeIdFromToken(token.substring(7));
            String notes = requestBody.getOrDefault("notes", "");

            BusinessTravelRequest approvedRequest = businessTravelRequestService.approveBusinessTravelRequest(requestId, supervisorId, notes);

            return ResponseEntity.ok(Map.of(
                "message", "Business travel request approved successfully",
                "travelRequest", approvedRequest
            ));

        } catch (Exception e) {
            log.error("Approve business travel request failed", e);
            return ResponseEntity.badRequest().body(
                Map.of("message", e.getMessage())
            );
        }
    }

    @PostMapping("/supervisor/reject/{requestId}")
    @PreAuthorize("hasRole('SUPERVISOR')")
    public ResponseEntity<?> rejectBusinessTravelRequest(@RequestHeader("Authorization") String token,
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

            BusinessTravelRequest rejectedRequest = businessTravelRequestService.rejectBusinessTravelRequest(requestId, supervisorId, rejectionReason);

            return ResponseEntity.ok(Map.of(
                "message", "Business travel request rejected successfully",
                "travelRequest", rejectedRequest
            ));

        } catch (Exception e) {
            log.error("Reject business travel request failed", e);
            return ResponseEntity.badRequest().body(
                Map.of("message", e.getMessage())
            );
        }
    }
}