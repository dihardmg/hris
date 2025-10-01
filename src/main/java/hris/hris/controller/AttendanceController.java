package hris.hris.controller;

import hris.hris.dto.ClockInRequest;
import hris.hris.model.Attendance;
import hris.hris.security.JwtUtil;
import hris.hris.service.AttendanceService;
import hris.hris.service.AuthenticationService;
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
@RequestMapping("/api/attendance")
@CrossOrigin(origins = "*", maxAge = 3600)
@Slf4j
public class AttendanceController {

    @Autowired
    private AttendanceService attendanceService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AuthenticationService authenticationService;

    @PostMapping("/clock-in")
    public ResponseEntity<?> clockIn(@RequestHeader("Authorization") String token,
                                    @Valid @RequestBody ClockInRequest request) {
        try {
            String email = getEmailFromToken(token);
            Long employeeId = jwtUtil.getEmployeeIdFromToken(token.substring(7));

            if (attendanceService.isClockedIn(employeeId)) {
                return ResponseEntity.badRequest().body(
                    Map.of("message", "Already clocked in today")
                );
            }

            Attendance attendance = attendanceService.clockIn(employeeId, request);

            return ResponseEntity.ok(Map.of(
                "message", "Clock in successful",
                "attendance", attendance,
                "isWithinGeofence", attendance.getIsWithinGeofence(),
                "faceRecognitionConfidence", attendance.getFaceRecognitionConfidence()
            ));

        } catch (Exception e) {
            log.error("Clock in failed", e);
            return ResponseEntity.badRequest().body(
                Map.of("message", e.getMessage())
            );
        }
    }

    @PostMapping("/clock-out")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<?> clockOut(@RequestHeader("Authorization") String token) {
        try {
            Long employeeId = jwtUtil.getEmployeeIdFromToken(token.substring(7));

            if (!attendanceService.isClockedIn(employeeId)) {
                return ResponseEntity.badRequest().body(
                    Map.of("message", "No active clock-in record found")
                );
            }

            Attendance attendance = attendanceService.clockOut(employeeId);

            return ResponseEntity.ok(Map.of(
                "message", "Clock out successful",
                "attendance", attendance
            ));

        } catch (Exception e) {
            log.error("Clock out failed", e);
            return ResponseEntity.badRequest().body(
                Map.of("message", e.getMessage())
            );
        }
    }

    @GetMapping("/today")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<?> getTodayAttendance(@RequestHeader("Authorization") String token) {
        try {
            Long employeeId = jwtUtil.getEmployeeIdFromToken(token.substring(7));

            Optional<Attendance> attendance = attendanceService.getTodayAttendance(employeeId);

            if (attendance.isPresent()) {
                return ResponseEntity.ok(attendance.get());
            } else {
                return ResponseEntity.ok(Map.of("message", "No attendance record for today"));
            }

        } catch (Exception e) {
            log.error("Get today attendance failed", e);
            return ResponseEntity.badRequest().body(
                Map.of("message", "Failed to get today's attendance")
            );
        }
    }

    @GetMapping("/status")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<?> getAttendanceStatus(@RequestHeader("Authorization") String token) {
        try {
            Long employeeId = jwtUtil.getEmployeeIdFromToken(token.substring(7));

            boolean isClockedIn = attendanceService.isClockedIn(employeeId);
            Optional<Attendance> todayAttendance = attendanceService.getTodayAttendance(employeeId);

            return ResponseEntity.ok(Map.of(
                "isClockedIn", isClockedIn,
                "hasTodayRecord", todayAttendance.isPresent()
            ));

        } catch (Exception e) {
            log.error("Get attendance status failed", e);
            return ResponseEntity.badRequest().body(
                Map.of("message", "Failed to get attendance status")
            );
        }
    }

    @GetMapping("/history")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<?> getAttendanceHistory(@RequestHeader("Authorization") String token,
                                                 @RequestParam(defaultValue = "30") int days) {
        try {
            Long employeeId = jwtUtil.getEmployeeIdFromToken(token.substring(7));

            List<Attendance> history = attendanceService.getEmployeeAttendanceHistory(employeeId, days);

            return ResponseEntity.ok(history);

        } catch (Exception e) {
            log.error("Get attendance history failed", e);
            return ResponseEntity.badRequest().body(
                Map.of("message", "Failed to get attendance history")
            );
        }
    }

    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HR')")
    public ResponseEntity<?> getAllAttendance(@RequestParam(required = false) Long employeeId,
                                             @RequestParam(required = false) String date) {
        try {
            return ResponseEntity.ok(Map.of("message", "Admin endpoint - implement as needed"));
        } catch (Exception e) {
            log.error("Get all attendance failed", e);
            return ResponseEntity.badRequest().body(
                Map.of("message", "Failed to get attendance records")
            );
        }
    }

    private String getEmailFromToken(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            String jwtToken = token.substring(7);
            return jwtUtil.getEmailFromToken(jwtToken);
        }
        throw new RuntimeException("Invalid token");
    }
}