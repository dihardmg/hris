package hris.hris.service;

import hris.hris.dto.ClockInRequest;
import hris.hris.model.Attendance;
import hris.hris.model.Employee;
import hris.hris.repository.AttendanceRepository;
import hris.hris.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AttendanceServiceTest {

    @Mock
    private AttendanceRepository attendanceRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private FaceRecognitionService faceRecognitionService;

    @Mock
    private GeofencingService geofencingService;

    @InjectMocks
    private AttendanceService attendanceService;

    private Employee employee;
    private ClockInRequest clockInRequest;
    private Attendance existingAttendance;

    @BeforeEach
    void setUp() {
        employee = new Employee();
        employee.setId(1L);
        employee.setEmployeeId("EMP001");
        employee.setFirstName("John");
        employee.setLastName("Doe");
        employee.setEmail("john.doe@example.com");

        clockInRequest = new ClockInRequest();
        clockInRequest.setLatitude(-6.2088);
        clockInRequest.setLongitude(106.8456);
        clockInRequest.setLocationAddress("Office Location");
        clockInRequest.setFaceImage("base64FaceImage");
        clockInRequest.setNotes("Working from office");

        existingAttendance = new Attendance();
        existingAttendance.setId(1L);
        existingAttendance.setEmployee(employee);
        existingAttendance.setClockInTime(LocalDateTime.now().minusHours(8));
    }

    @Test
    void clockIn_WithValidRequest_ShouldReturnAttendance() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(attendanceRepository.findTodayAttendanceWithLock(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Optional.empty());
        when(geofencingService.isWithinGeofence(anyDouble(), anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(true);
        when(attendanceRepository.save(any(Attendance.class))).thenReturn(existingAttendance);

        Attendance result = attendanceService.clockIn(1L, clockInRequest);

        assertNotNull(result);

        verify(employeeRepository).findById(1L);
        verify(attendanceRepository).findTodayAttendanceWithLock(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class));
        verify(geofencingService).isWithinGeofence(
                -6.2088, 106.8456, -6.2088, 106.8456, 100.0
        );
        verify(attendanceRepository).save(any(Attendance.class));
    }

    @Test
    void clockIn_WithNonExistentEmployee_ShouldThrowRuntimeException() {
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> attendanceService.clockIn(999L, clockInRequest)
        );

        assertEquals("Employee not found", exception.getMessage());

        verify(employeeRepository).findById(999L);
        verify(attendanceRepository, never()).save(any(Attendance.class));
    }

    @Test
    void clockIn_WithExistingAttendanceToday_ShouldThrowRuntimeException() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(attendanceRepository.findTodayAttendanceWithLock(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Optional.of(existingAttendance));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> attendanceService.clockIn(1L, clockInRequest)
        );

        assertEquals("Already clocked in today", exception.getMessage());

        verify(employeeRepository).findById(1L);
        verify(attendanceRepository).findTodayAttendanceWithLock(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class));
        verify(attendanceRepository, never()).save(any(Attendance.class));
    }

    @Test
    void clockIn_WithoutLocation_ShouldStillWork() {
        ClockInRequest requestWithoutLocation = new ClockInRequest();
        requestWithoutLocation.setLatitude(0.0);
        requestWithoutLocation.setLongitude(0.0);
        requestWithoutLocation.setLocationAddress("Office Location");
        requestWithoutLocation.setFaceImage("base64FaceImage");
        requestWithoutLocation.setNotes("Working from office");

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(attendanceRepository.findTodayAttendanceWithLock(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Optional.empty());
        when(geofencingService.isWithinGeofence(anyDouble(), anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(false);
        when(attendanceRepository.save(any(Attendance.class))).thenReturn(existingAttendance);

        Attendance result = attendanceService.clockIn(1L, requestWithoutLocation);

        assertNotNull(result);

        verify(employeeRepository).findById(1L);
        verify(attendanceRepository).findTodayAttendanceWithLock(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class));
        verify(geofencingService).isWithinGeofence(anyDouble(), anyDouble(), anyDouble(), anyDouble(), anyDouble());
        verify(attendanceRepository).save(any(Attendance.class));
    }

    @Test
    void clockOut_WithExistingClockIn_ShouldReturnAttendance() {
        when(attendanceRepository.findTodayAttendanceWithLock(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Optional.of(existingAttendance));
        when(attendanceRepository.save(any(Attendance.class))).thenReturn(existingAttendance);

        Attendance result = attendanceService.clockOut(1L);

        assertNotNull(result);
        assertNotNull(result.getClockOutTime());

        verify(attendanceRepository).findTodayAttendanceWithLock(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class));
        verify(attendanceRepository).save(existingAttendance);
    }

    @Test
    void clockOut_WithNoClockInToday_ShouldThrowRuntimeException() {
        when(attendanceRepository.findTodayAttendanceWithLock(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> attendanceService.clockOut(1L)
        );

        assertEquals("No clock-in record found for today", exception.getMessage());

        verify(attendanceRepository).findTodayAttendanceWithLock(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class));
        verify(attendanceRepository, never()).save(any(Attendance.class));
    }

    @Test
    void clockOut_WithAlreadyClockedOut_ShouldThrowRuntimeException() {
        existingAttendance.setClockOutTime(LocalDateTime.now().minusHours(4));

        when(attendanceRepository.findTodayAttendanceWithLock(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Optional.of(existingAttendance));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> attendanceService.clockOut(1L)
        );

        assertEquals("Already clocked out today", exception.getMessage());

        verify(attendanceRepository).findTodayAttendanceWithLock(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class));
        verify(attendanceRepository, never()).save(any(Attendance.class));
    }

    @Test
    void getEmployeeAttendanceHistory_ShouldReturnAttendanceList() {
        List<Attendance> attendances = Arrays.asList(existingAttendance);
        when(attendanceRepository.findByEmployeeIdAndClockInTimeBetweenOrderByCreatedAtDesc(
                anyLong(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(attendances);

        List<Attendance> result = attendanceService.getEmployeeAttendanceHistory(1L, 7);

        assertNotNull(result);
        assertEquals(1, result.size());

        verify(attendanceRepository).findByEmployeeIdAndClockInTimeBetweenOrderByCreatedAtDesc(
                eq(1L), any(LocalDateTime.class), any(LocalDateTime.class)
        );
    }

    @Test
    void getTodayAttendance_WithExistingAttendance_ShouldReturnAttendance() {
        when(attendanceRepository.findTodayAttendance(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Optional.of(existingAttendance));

        Optional<Attendance> result = attendanceService.getTodayAttendance(1L);

        assertTrue(result.isPresent());
        assertEquals("EMP001", result.get().getEmployee().getEmployeeId());

        verify(attendanceRepository).findTodayAttendance(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    void getTodayAttendance_WithNoAttendance_ShouldReturnEmpty() {
        when(attendanceRepository.findTodayAttendance(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Optional.empty());

        Optional<Attendance> result = attendanceService.getTodayAttendance(1L);

        assertFalse(result.isPresent());

        verify(attendanceRepository).findTodayAttendance(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    void isClockedIn_WithActiveAttendance_ShouldReturnTrue() {
        when(attendanceRepository.existsByEmployeeIdAndClockOutTimeIsNull(1L))
                .thenReturn(true);

        boolean result = attendanceService.isClockedIn(1L);

        assertTrue(result);

        verify(attendanceRepository).existsByEmployeeIdAndClockOutTimeIsNull(1L);
    }

    @Test
    void isClockedIn_WithNoActiveAttendance_ShouldReturnFalse() {
        when(attendanceRepository.existsByEmployeeIdAndClockOutTimeIsNull(1L))
                .thenReturn(false);

        boolean result = attendanceService.isClockedIn(1L);

        assertFalse(result);

        verify(attendanceRepository).existsByEmployeeIdAndClockOutTimeIsNull(1L);
    }
}