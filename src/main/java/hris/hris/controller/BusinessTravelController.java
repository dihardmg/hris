package hris.hris.controller;

import hris.hris.dto.BusinessTravelRequestDto;
import hris.hris.dto.BusinessTravelRequestRequestDto;
import hris.hris.dto.BusinessTravelRequestResponseDto;
import hris.hris.dto.CityDropdownDto;
import hris.hris.dto.PaginatedBusinessTravelRequestResponse;
import hris.hris.dto.PaginatedCityDropdownResponse;
import hris.hris.dto.PageInfo;
import hris.hris.model.BusinessTravelRequest;
import hris.hris.security.JwtUtil;
import hris.hris.service.BusinessTravelRequestService;
import hris.hris.service.CityService;
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
import java.util.LinkedHashMap;
import java.util.UUID;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/business-travel")
@CrossOrigin(origins = "*", maxAge = 3600)
@Slf4j
public class BusinessTravelController {

    @Autowired
    private BusinessTravelRequestService businessTravelRequestService;

    @Autowired
    private CityService cityService;

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping("/cities")
    @PreAuthorize("hasRole('EMPLOYEE') or hasRole('SUPERVISOR') or hasRole('HR') or hasRole('ADMIN')")
    public ResponseEntity<PaginatedCityDropdownResponse> getCitiesDropdown(
            @RequestHeader("Authorization") String token,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<CityDropdownDto> citiesPage;
            if (search != null && !search.trim().isEmpty()) {
                citiesPage = cityService.searchCities(search.trim(), pageable);
            } else {
                citiesPage = cityService.getAllActiveCitiesForDropdown(pageable);
            }

            PageInfo pageInfo = PageInfo.builder()
                    .size(citiesPage.getSize())
                    .total(citiesPage.getTotalElements())
                    .totalPages(citiesPage.getTotalPages())
                    .current(citiesPage.getNumber() + 1)
                    .build();

            PaginatedCityDropdownResponse response = PaginatedCityDropdownResponse.builder()
                    .data(citiesPage.getContent())
                    .page(pageInfo)
                    .build();
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Get cities dropdown failed", e);
            return ResponseEntity.badRequest().body(new PaginatedCityDropdownResponse());
        }
    }

    @PostMapping("/request")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<Object> createBusinessTravelRequest(
            @RequestHeader("Authorization") String token,
            @RequestBody BusinessTravelRequestRequestDto requestDto) {
        try {
            // Manual validation with custom response format
            Map<String, Object> errors = validateBusinessTravelRequest(requestDto);

            if (!errors.isEmpty()) {
                // Use LinkedHashMap to maintain insertion order
                Map<String, Object> response = new LinkedHashMap<>();
                response.put("code", "400");
                response.put("status", "BAD_REQUEST");
                response.put("errors", errors);

                // Log the response for debugging
                log.info("Validation error response: {}", response);

                return ResponseEntity.badRequest().body(response);
            }

            Long employeeId = jwtUtil.getEmployeeIdFromToken(token.substring(7));

            // Convert request DTO to service DTO with proper LocalDate fields
            BusinessTravelRequestDto serviceDto = new BusinessTravelRequestDto();
            serviceDto.setCityId(requestDto.getCityId());
            serviceDto.setStartDateFromString(requestDto.getStartDate());
            serviceDto.setEndDateFromString(requestDto.getEndDate());
            serviceDto.setReason(requestDto.getReason());

            BusinessTravelRequest travelRequest = businessTravelRequestService.createBusinessTravelRequest(employeeId, serviceDto);

            return ResponseEntity.status(201).body(Map.of(
                "success", true,
                "message", "Business travel request submitted successfully",
                "data", BusinessTravelRequestResponseDto.fromBusinessTravelRequest(travelRequest)
            ));

        } catch (Exception e) {
            log.error("Create business travel request failed", e);
            String errorMessage = e.getMessage() != null ? e.getMessage() : "Unknown error occurred";
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", errorMessage));
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
    public ResponseEntity<?> getPendingBusinessTravelRequests(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        try {
            Long supervisorId = jwtUtil.getEmployeeIdFromToken(token.substring(7));
            Pageable pageable = PageRequest.of(page, size);
            Page<BusinessTravelRequest> pendingRequestsPage = businessTravelRequestService.getPendingRequestsForSupervisor(supervisorId, pageable);

            List<BusinessTravelRequestResponseDto> pendingRequestDtos = pendingRequestsPage.getContent().stream()
                .map(BusinessTravelRequestResponseDto::fromBusinessTravelRequest)
                .toList();

            PaginatedBusinessTravelRequestResponse response = PaginatedBusinessTravelRequestResponse.createResponse(
                pendingRequestDtos,
                pendingRequestsPage.getSize(),
                (int) pendingRequestsPage.getTotalElements(),
                pendingRequestsPage.getTotalPages(),
                pendingRequestsPage.getNumber() + 1
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Get pending business travel requests failed", e);
            PaginatedBusinessTravelRequestResponse errorResponse = PaginatedBusinessTravelRequestResponse.createResponse(
                List.of(), 0, 0, 0, 0
            );
            return ResponseEntity.badRequest().body(errorResponse);
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

    private Map<String, Object> validateBusinessTravelRequest(BusinessTravelRequestRequestDto requestDto) {
        Map<String, Object> errors = new LinkedHashMap<>();

        // Validate cityId
        java.util.List<String> cityIdErrors = new java.util.ArrayList<>();
        if (requestDto.getCityId() == null) {
            cityIdErrors.add("must be not null");
        } else if (requestDto.getCityId() <= 0) {
            cityIdErrors.add("only number int");
        }
        if (!cityIdErrors.isEmpty()) {
            errors.put("cityId", cityIdErrors.toArray(new String[0]));
        }

        // Validate startDate
        LocalDate startDate = requestDto.getStartDateAsDate();
        if (requestDto.getStartDate() == null || requestDto.getStartDate().trim().isEmpty()) {
            errors.put("startDate", new String[]{"must be not null"});
        } else if (startDate == null) {
            // Invalid date format
            errors.put("startDate", new String[]{"format not valid e.g 2025-10-11"});
        } else {
            java.util.List<String> startDateErrors = new java.util.ArrayList<>();
            if (startDate.getYear() < 2000 || startDate.getYear() > 2100) {
                startDateErrors.add("format not valid e.g 2025-10-11");
            }
            if (startDate.isBefore(LocalDate.now())) {
                startDateErrors.add("must be today or future date");
            }
            if (!startDateErrors.isEmpty()) {
                errors.put("startDate", startDateErrors.toArray(new String[0]));
            }
        }

        // Validate endDate
        LocalDate endDate = requestDto.getEndDateAsDate();
        if (requestDto.getEndDate() == null || requestDto.getEndDate().trim().isEmpty()) {
            errors.put("endDate", new String[]{"must be not null"});
        } else if (endDate == null) {
            // Invalid date format
            errors.put("endDate", new String[]{"format not valid e.g 2025-10-11"});
        } else {
            java.util.List<String> endDateErrors = new java.util.ArrayList<>();
            if (endDate.getYear() < 2000 || endDate.getYear() > 2100) {
                endDateErrors.add("format not valid e.g 2025-10-11");
            }
            if (endDate.isBefore(LocalDate.now())) {
                endDateErrors.add("must be today or future date");
            }
            if (!endDateErrors.isEmpty()) {
                errors.put("endDate", endDateErrors.toArray(new String[0]));
            }
        }

        // Validate date range if both dates are present and valid
        if (startDate != null && endDate != null) {
            java.util.List<String> dateRangeErrors = new java.util.ArrayList<>();

            if (endDate.isBefore(startDate)) {
                dateRangeErrors.add("must be after start date");
            }

            long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate);
            if (daysBetween > 365) {
                dateRangeErrors.add("travel period cannot exceed 365 days");
            }

            if (!dateRangeErrors.isEmpty()) {
                // Merge with existing endDate errors if any
                java.util.List<String> existingEndDateErrors = new java.util.ArrayList<>();
                if (errors.containsKey("endDate")) {
                    String[] existing = (String[]) errors.get("endDate");
                    existingEndDateErrors = new java.util.ArrayList<>(java.util.Arrays.asList(existing));
                }
                existingEndDateErrors.addAll(dateRangeErrors);
                errors.put("endDate", existingEndDateErrors.toArray(new String[0]));
            }
        }

        return errors;
    }
}