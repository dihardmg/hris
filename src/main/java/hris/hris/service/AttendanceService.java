package hris.hris.service;

import hris.hris.dto.ClockInRequest;
import hris.hris.model.Attendance;
import hris.hris.model.Employee;
import hris.hris.repository.AttendanceRepository;
import hris.hris.repository.EmployeeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class AttendanceService {

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private FaceRecognitionService faceRecognitionService;

    @Autowired
    private GeofencingService geofencingService;

    private static final double OFFICE_LATITUDE = -6.2088;
    private static final double OFFICE_LONGITUDE = 106.8456;
    private static final double GEO_FENCE_RADIUS = 100.0;

    @Transactional
    public Attendance clockIn(Long employeeId, ClockInRequest request) {
        Employee employee = employeeRepository.findById(employeeId)
            .orElseThrow(() -> new RuntimeException("Employee not found"));

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = now.toLocalDate().atTime(LocalTime.MAX);

        Optional<Attendance> existingAttendance = attendanceRepository
            .findTodayAttendanceWithLock(employeeId, startOfDay, endOfDay);

        if (existingAttendance.isPresent()) {
            throw new RuntimeException("Already clocked in today");
        }

        Double faceConfidence = null;
        if (request.getFaceImage() != null) {
            // Face template verification temporarily disabled
            log.debug("Face image provided but verification is disabled (feature temporarily unavailable)");
            // Face verification would be implemented here when face templates are re-enabled
            faceConfidence = 0.8; // Default confidence for now
        }

        boolean isWithinGeofence = geofencingService.isWithinGeofence(
            request.getLatitude(),
            request.getLongitude(),
            OFFICE_LATITUDE,
            OFFICE_LONGITUDE,
            GEO_FENCE_RADIUS
        );

        Attendance attendance = new Attendance();
        attendance.setEmployee(employee);
        attendance.setClockInTime(now);
        attendance.setLatitude(request.getLatitude() != null ? BigDecimal.valueOf(request.getLatitude()) : null);
        attendance.setLongitude(request.getLongitude() != null ? BigDecimal.valueOf(request.getLongitude()) : null);
        attendance.setLocationAddress(request.getLocationAddress());
        attendance.setIsWithinGeofence(isWithinGeofence);
        attendance.setFaceRecognitionConfidence(faceConfidence != null ? BigDecimal.valueOf(faceConfidence) : null);
        attendance.setNotes(request.getNotes());

        Attendance savedAttendance = attendanceRepository.save(attendance);
        log.info("Employee {} clocked in at {}", employee.getEmployeeId(), now);

        return savedAttendance;
    }

    @Transactional
    public Attendance clockOut(Long employeeId) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = now.toLocalDate().atTime(LocalTime.MAX);

        Optional<Attendance> attendanceOpt = attendanceRepository
            .findTodayAttendanceWithLock(employeeId, startOfDay, endOfDay);

        if (attendanceOpt.isEmpty()) {
            throw new RuntimeException("No clock-in record found for today");
        }

        Attendance attendance = attendanceOpt.get();
        if (attendance.getClockOutTime() != null) {
            throw new RuntimeException("Already clocked out today");
        }

        attendance.setClockOutTime(now);
        attendanceRepository.save(attendance);

        log.info("Employee {} clocked out at {}",
                attendance.getEmployee().getEmployeeId(), now);

        return attendance;
    }

    @Transactional(readOnly = true)
    public List<Attendance> getEmployeeAttendanceHistory(Long employeeId, int days) {
        LocalDateTime startDate = LocalDateTime.now().minusDays(days);
        LocalDateTime endDate = LocalDateTime.now();

        return attendanceRepository.findByEmployeeIdAndClockInTimeBetweenOrderByCreatedAtDesc(
            employeeId, startDate, endDate);
    }

    @Transactional(readOnly = true)
    public Optional<Attendance> getTodayAttendance(Long employeeId) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = now.toLocalDate().atTime(LocalTime.MAX);

        return attendanceRepository.findTodayAttendance(employeeId, startOfDay, endOfDay);
    }

    @Transactional(readOnly = true)
    public boolean isClockedIn(Long employeeId) {
        return attendanceRepository.existsByEmployeeIdAndClockOutTimeIsNull(employeeId);
    }
}