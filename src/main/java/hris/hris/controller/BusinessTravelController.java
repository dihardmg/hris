package hris.hris.controller;

import hris.hris.dto.BusinessTravelRequestDto;
import hris.hris.dto.BusinessTravelRequestResponseDto;
import hris.hris.dto.PaginatedBusinessTravelRequestResponse;
import hris.hris.model.BusinessTravelRequest;
import hris.hris.security.JwtUtil;
import hris.hris.service.BusinessTravelRequestService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

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
                "data", BusinessTravelRequestResponseDto.fromBusinessTravelRequest(travelRequest)
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
    public ResponseEntity<PaginatedBusinessTravelRequestResponse> getMyBusinessTravelRequests(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Long employeeId = jwtUtil.getEmployeeIdFromToken(token.substring(7));

            // Create pageable request
            Pageable pageable = PageRequest.of(page, size);
            Page<BusinessTravelRequest> requestsPage = businessTravelRequestService.getEmployeeBusinessTravelRequests(employeeId, pageable);

            // Convert to DTOs to avoid lazy loading issues
            List<BusinessTravelRequestResponseDto> requestDtos = requestsPage.getContent().stream()
                .map(BusinessTravelRequestResponseDto::fromBusinessTravelRequest)
                .toList();

            // Create paginated response with real pagination data
            PaginatedBusinessTravelRequestResponse response = PaginatedBusinessTravelRequestResponse.createResponse(
                requestDtos,
                requestsPage.getSize(),         // size per page
                (int) requestsPage.getTotalElements(), // total elements
                requestsPage.getTotalPages(),    // total pages
                requestsPage.getNumber() + 1     // current page (convert from 0-based to 1-based)
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Get business travel requests failed", e);
            // Return empty paginated response for error case
            PaginatedBusinessTravelRequestResponse errorResponse = PaginatedBusinessTravelRequestResponse.createResponse(
                List.of(),      // empty data
                0,              // size
                0,              // total
                0,              // totalPages
                0               // currentPage
            );
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @GetMapping("/request/{uuid}")
    @PreAuthorize("hasRole('EMPLOYEE') or hasRole('SUPERVISOR') or hasRole('HR') or hasRole('ADMIN')")
    public ResponseEntity<?> getBusinessTravelRequestByUuid(@RequestHeader("Authorization") String token,
                                                         @PathVariable UUID uuid) {
        try {
            Long employeeId = jwtUtil.getEmployeeIdFromToken(token.substring(7));

            var travelRequestOptional = businessTravelRequestService.getBusinessTravelRequestByUuid(uuid);
            if (travelRequestOptional.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Business travel request not found"));
            }

            BusinessTravelRequest travelRequest = travelRequestOptional.get();
            BusinessTravelRequestResponseDto responseDto = BusinessTravelRequestResponseDto.fromBusinessTravelRequest(travelRequest);

            return ResponseEntity.ok(Map.of(
                "data", responseDto,
                "message", "Travel record retrieved successfully"
            ));

        } catch (Exception e) {
            log.error("Get business travel request by UUID failed", e);
            return ResponseEntity.badRequest().body(
                Map.of("message", "Failed to get business travel request")
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
                List<BusinessTravelRequestResponseDto> currentTravelDtos = currentTravel.stream()
                    .map(BusinessTravelRequestResponseDto::fromBusinessTravelRequest)
                    .toList();
                return ResponseEntity.ok(currentTravelDtos);
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
            List<BusinessTravelRequestResponseDto> pendingRequestDtos = pendingRequests.stream()
                .map(BusinessTravelRequestResponseDto::fromBusinessTravelRequest)
                .toList();

            return ResponseEntity.ok(pendingRequestDtos);

        } catch (Exception e) {
            log.error("Get pending business travel requests failed", e);
            return ResponseEntity.badRequest().body(
                Map.of("message", "Failed to get pending business travel requests")
            );
        }
    }

    @PostMapping("/supervisor/approve/{uuid}")
    @PreAuthorize("hasRole('SUPERVISOR')")
    public ResponseEntity<?> approveBusinessTravelRequest(@RequestHeader("Authorization") String token,
                                                        @PathVariable UUID uuid,
                                                        @RequestBody(required = false) Map<String, String> requestBody) {
        try {
            Long supervisorId = jwtUtil.getEmployeeIdFromToken(token.substring(7));

            BusinessTravelRequest approvedRequest = businessTravelRequestService.approveBusinessTravelRequest(uuid, supervisorId, null);

            return ResponseEntity.ok(Map.of(
                "message", "Business travel request approved successfully",
                "data", BusinessTravelRequestResponseDto.fromBusinessTravelRequest(approvedRequest)
            ));

        } catch (Exception e) {
            log.error("Approve business travel request failed", e);
            return ResponseEntity.badRequest().body(
                Map.of("message", e.getMessage())
            );
        }
    }

    @PostMapping("/supervisor/reject/{uuid}")
    @PreAuthorize("hasRole('SUPERVISOR')")
    public ResponseEntity<?> rejectBusinessTravelRequest(@RequestHeader("Authorization") String token,
                                                         @PathVariable UUID uuid,
                                                         @RequestBody(required = false) Map<String, String> requestBody) {
        try {
            Long supervisorId = jwtUtil.getEmployeeIdFromToken(token.substring(7));

            BusinessTravelRequest rejectedRequest = businessTravelRequestService.rejectBusinessTravelRequest(uuid, supervisorId, null);

            return ResponseEntity.ok(Map.of(
                "message", "Business travel request rejected successfully",
                "data", BusinessTravelRequestResponseDto.fromBusinessTravelRequest(rejectedRequest)
            ));

        } catch (Exception e) {
            log.error("Reject business travel request failed", e);
            return ResponseEntity.badRequest().body(
                Map.of("message", e.getMessage())
            );
        }
    }
}