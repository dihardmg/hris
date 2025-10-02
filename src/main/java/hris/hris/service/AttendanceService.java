package hris.hris.service;

import hris.hris.dto.AttendanceDto;
import hris.hris.dto.ClockInRequest;
import hris.hris.model.Attendance;
import hris.hris.model.Employee;
import hris.hris.repository.AttendanceRepository;
import hris.hris.repository.EmployeeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

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
    public AttendanceDto clockIn(Long employeeId, ClockInRequest request) {
        Employee employee = employeeRepository.findById(employeeId)
            .orElseThrow(() -> new RuntimeException("Employee not found"));

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = now.toLocalDate().atTime(LocalTime.MAX);

        Optional<Attendance> existingAttendance = attendanceRepository
            .findTodayAttendanceWithLock(employeeId, startOfDay, endOfDay);

        if (existingAttendance.isPresent()) {
            Attendance attendance = existingAttendance.get();
            if (attendance.getClockOutTime() != null) {
                throw new RuntimeException("You have already clocked in and clocked out today");
            } else {
                throw new RuntimeException("Already clocked in today");
            }
        }

        Double faceConfidence = null;
        if (request.getFaceImage() != null) {
            log.debug("Face image provided but verification is disabled (feature temporarily unavailable)");
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

        return mapToDto(savedAttendance);
    }

    @Transactional
    public AttendanceDto clockOut(Long employeeId) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = now.toLocalDate().atTime(LocalTime.MAX);

        Optional<Attendance> attendanceOpt = attendanceRepository
            .findTodayAttendanceWithLock(employeeId, startOfDay, endOfDay);

        if (attendanceOpt.isEmpty()) {
            throw new RuntimeException("No active clock-in record found for today");
        }

        Attendance attendance = attendanceOpt.get();
        if (attendance.getClockOutTime() != null) {
            throw new RuntimeException("You have already clocked out today");
        }

        attendance.setClockOutTime(now);
        Attendance savedAttendance = attendanceRepository.save(attendance);

        log.info("Employee {} clocked out at {}",
                savedAttendance.getEmployee().getEmployeeId(), now);

        return mapToDto(savedAttendance);
    }

    @Transactional(readOnly = true)
    public List<AttendanceDto> getEmployeeAttendanceHistory(Long employeeId, int days) {
        LocalDateTime startDate = LocalDateTime.now().minusDays(days);
        LocalDateTime endDate = LocalDateTime.now();

        return attendanceRepository.findByEmployeeIdAndClockInTimeBetweenOrderByCreatedAtDesc(
            employeeId, startDate, endDate).stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<AttendanceDto> getEmployeeAttendanceHistoryPaginated(Long employeeId, int days, int page, int size) {
        LocalDateTime startDate = LocalDateTime.now().minusDays(days);
        LocalDateTime endDate = LocalDateTime.now();
        Pageable pageable = PageRequest.of(page, size);

        Page<Attendance> attendancePage = attendanceRepository.findByEmployeeIdAndClockInTimeBetweenOrderByCreatedAtDesc(
            employeeId, startDate, endDate, pageable);

        return attendancePage.map(this::mapToDto);
    }

    @Transactional(readOnly = true)
    public Optional<AttendanceDto> getTodayAttendance(Long employeeId) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = now.toLocalDate().atTime(LocalTime.MAX);

        return attendanceRepository.findTodayAttendance(employeeId, startOfDay, endOfDay).map(this::mapToDto);
    }

    @Transactional(readOnly = true)
    public boolean isClockedIn(Long employeeId) {
        return attendanceRepository.existsByEmployeeIdAndClockOutTimeIsNull(employeeId);
    }

    @Transactional(readOnly = true)
    public boolean hasCompletedAttendance(Long employeeId, LocalDateTime date) {
        LocalDateTime startOfDay = date.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = date.toLocalDate().atTime(LocalTime.MAX);
        return attendanceRepository.existsByEmployeeIdAndClockInTimeBetweenAndClockOutTimeIsNotNull(
            employeeId, startOfDay, endOfDay);
    }

    @Transactional(readOnly = true)
    public boolean hasCompletedAttendanceToday(Long employeeId) {
        return hasCompletedAttendance(employeeId, LocalDateTime.now());
    }

    @Transactional(readOnly = true)
    public Optional<AttendanceDto> getAttendanceById(Long attendanceId) {
        return attendanceRepository.findById(attendanceId).map(this::mapToDto);
    }

    @Transactional(readOnly = true)
    public Optional<AttendanceDto> getAttendanceByUuid(UUID uuid) {
        return attendanceRepository.findByUuid(uuid).map(this::mapToDto);
    }

    public AttendanceDto mapToDto(Attendance attendance) {
        AttendanceDto dto = new AttendanceDto();
        dto.setUuid(attendance.getUuid());
        dto.setEmployeeName(attendance.getEmployee().getFirstName() + " " + attendance.getEmployee().getLastName());
        dto.setClockInTime(attendance.getClockInTime());
        dto.setClockOutTime(attendance.getClockOutTime());
        dto.setLatitude(attendance.getLatitude());
        dto.setLongitude(attendance.getLongitude());
        dto.setLocationAddress(attendance.getLocationAddress());
        dto.setWithinGeofence(attendance.getIsWithinGeofence() != null ? attendance.getIsWithinGeofence() : false);
        dto.setFaceRecognitionConfidence(attendance.getFaceRecognitionConfidence());
        dto.setNotes(attendance.getNotes());
        return dto;
    }
}
