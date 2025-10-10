package hris.hris.controller;

import hris.hris.dto.ApiResponse;
import hris.hris.dto.AttendanceDto;
import hris.hris.dto.ClockInRequest;
import hris.hris.dto.ErrorResponse;
import hris.hris.dto.PaginatedAttendanceResponse;
import hris.hris.exception.AttendanceException;
import hris.hris.model.Attendance;
import hris.hris.model.Employee;
import hris.hris.repository.AttendanceRepository;
import hris.hris.repository.EmployeeRepository;
import hris.hris.security.JwtUtil;
import hris.hris.service.AttendanceService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/attendance")
@CrossOrigin(origins = "*", maxAge = 3600)
@Slf4j
public class AttendanceController {

    @Autowired
    private AttendanceService attendanceService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private AttendanceRepository attendanceRepository;

    private Long getEmployeeIdFromAuth() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return employeeRepository.findByEmail(username)
                .map(Employee::getId)
                .orElseThrow(() -> new RuntimeException("Employee not found: " + username));
    }

    @PostMapping("/clock-in")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<?> clockIn(@Valid @RequestBody ClockInRequest request) {
        try {
            Long employeeId = getEmployeeIdFromAuth();
            AttendanceDto attendanceDto = attendanceService.clockIn(employeeId, request);
            return ResponseEntity.status(201).body(ApiResponse.success(attendanceDto, "Clock in successful"));
        } catch (IllegalStateException e) {
            log.warn("Clock in business logic violation: {}", e.getMessage());
            return ResponseEntity.status(409).body(Map.of(
                "message", e.getMessage(),
                "error", "CLOCK_IN_VIOLATION",
                "timestamp", java.time.Instant.now().toString()
            ));
        } catch (Exception e) {
            log.error("Clock in failed for user", e);
            return ResponseEntity.badRequest().body(Map.of(
                "message", "Clock in failed: " + e.getMessage(),
                "error", "CLOCK_IN_ERROR",
                "timestamp", java.time.Instant.now().toString()
            ));
        }
    }

    @PostMapping("/clock-out")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<?> clockOut() {
        try {
            Long employeeId = getEmployeeIdFromAuth();
            AttendanceDto attendanceDto = attendanceService.clockOut(employeeId);
            return ResponseEntity.status(200).body(ApiResponse.success(attendanceDto, "Clock out successful"));
        } catch (IllegalStateException e) {
            log.warn("Clock out business logic violation: {}", e.getMessage());
            return ResponseEntity.status(409).body(Map.of(
                "message", e.getMessage(),
                "error", "CLOCK_OUT_VIOLATION",
                "timestamp", java.time.Instant.now().toString()
            ));
        } catch (Exception e) {
            log.error("Clock out failed for user", e);
            return ResponseEntity.badRequest().body(Map.of(
                "message", "Clock out failed: " + e.getMessage(),
                "error", "CLOCK_OUT_ERROR",
                "timestamp", java.time.Instant.now().toString()
            ));
        }
    }

    @GetMapping("/today")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<ApiResponse<AttendanceDto>> getTodayAttendance() {
        try {
            Long employeeId = getEmployeeIdFromAuth();
            Optional<AttendanceDto> attendanceDto = attendanceService.getTodayAttendance(employeeId);
            if (attendanceDto.isPresent()) {
                return ResponseEntity.ok(ApiResponse.success(attendanceDto.get()));
            } else {
                return ResponseEntity.ok(ApiResponse.success(null, "No attendance record for today"));
            }
        } catch (Exception e) {
            log.error("Get today attendance failed", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/status")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAttendanceStatus() {
        try {
            Long employeeId = getEmployeeIdFromAuth();
            boolean isClockedIn = attendanceService.isClockedIn(employeeId);
            Optional<AttendanceDto> todayAttendance = attendanceService.getTodayAttendance(employeeId);

            Map<String, Object> status = Map.of(
                "isClockedIn", isClockedIn,
                "hasClockedOut", todayAttendance.isPresent() && todayAttendance.get().getClockOutTime() != null
            );

            return ResponseEntity.ok(ApiResponse.success(status));
        } catch (Exception e) {
            log.error("Get attendance status failed", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/history")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<PaginatedAttendanceResponse> getAttendanceHistory(
            @RequestParam(defaultValue = "30") int days,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long employeeId = getEmployeeIdFromAuth();
        var history = attendanceService.getEmployeeAttendanceHistoryPaginated(employeeId, days, page, size);

        PaginatedAttendanceResponse response = PaginatedAttendanceResponse.createResponse(
            history.getContent(),
            history.getSize(),
            history.getTotalElements(),
            history.getTotalPages(),
            history.getNumber() + 1 // Convert 0-based to 1-based
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{uuid}")
    @PreAuthorize("hasRole('EMPLOYEE') or hasRole('SUPERVISOR') or hasRole('HR') or hasRole('ADMIN')")
    public ResponseEntity<?> getAttendanceByUuid(@PathVariable UUID uuid, HttpServletRequest request) {
        try {
            Long currentEmployeeId = getEmployeeIdFromAuth();

            var attendanceEntityOpt = attendanceRepository.findByUuid(uuid);
            if (attendanceEntityOpt.isEmpty()) {
                throw new AttendanceException(AttendanceException.AttendanceErrorType.NOT_FOUND, "Attendance data not found");
            }

            var attendanceEntity = attendanceEntityOpt.get();

            // Authorization check: Employees can only view their own attendance records,
            // Supervisors, HR, and Admin can view all records
            if (!currentEmployeeId.equals(attendanceEntity.getEmployee().getId())) {
                // Check if current user has supervisor, HR, or admin role
                Employee currentUser = employeeRepository.findById(currentEmployeeId)
                    .orElseThrow(() -> new RuntimeException("Current user not found"));

                String userEmail = currentUser.getEmail();
                boolean isSupervisorOrAbove = "supervisor@hris.com".equals(userEmail) ||
                                           "hr@hris.com".equals(userEmail) ||
                                           "admin@hris.com".equals(userEmail);

                if (!isSupervisorOrAbove) {
                    return ResponseEntity.status(403).body(ApiResponse.error("You are not authorized to view this attendance record"));
                }
            }

            AttendanceDto attendanceDto = attendanceService.mapToDto(attendanceEntity);
            return ResponseEntity.ok(ApiResponse.success(attendanceDto, "Attendance record retrieved successfully"));

        } catch (AttendanceException e) {
            throw e;
        } catch (Exception e) {
            log.error("Get attendance by UUID failed", e);
            String errorMessage = e.getMessage() != null ? e.getMessage() : "Failed to get attendance record";
            return ResponseEntity.badRequest().body(ApiResponse.error(errorMessage));
        }
    }
}
