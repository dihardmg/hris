package hris.hris.service;

import hris.hris.dto.AttendanceDto;
import hris.hris.dto.ClockInRequest;
import hris.hris.dto.PaginatedAttendanceResponse;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

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

        AttendanceDto result = attendanceService.clockIn(1L, clockInRequest);

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
    void clockIn_WithAlreadyClockedOut_ShouldThrowRuntimeException() {
        existingAttendance.setClockOutTime(LocalDateTime.now().minusHours(4));

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(attendanceRepository.findTodayAttendanceWithLock(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Optional.of(existingAttendance));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> attendanceService.clockIn(1L, clockInRequest)
        );

        assertEquals("You have already clocked in and clocked out today", exception.getMessage());

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

        AttendanceDto result = attendanceService.clockIn(1L, requestWithoutLocation);

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

        AttendanceDto result = attendanceService.clockOut(1L);

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

        assertEquals("No active clock-in record found for today", exception.getMessage());

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

        assertEquals("You have already clocked out today", exception.getMessage());

        verify(attendanceRepository).findTodayAttendanceWithLock(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class));
        verify(attendanceRepository, never()).save(any(Attendance.class));
    }

    @Test
    void getEmployeeAttendanceHistory_ShouldReturnAttendanceList() {
        List<Attendance> attendances = Arrays.asList(existingAttendance);
        when(attendanceRepository.findByEmployeeIdAndClockInTimeBetweenOrderByCreatedAtDesc(
                anyLong(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(attendances);

        List<AttendanceDto> result = attendanceService.getEmployeeAttendanceHistory(1L, 7);

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

        Optional<AttendanceDto> result = attendanceService.getTodayAttendance(1L);

        assertTrue(result.isPresent());

        verify(attendanceRepository).findTodayAttendance(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    void getTodayAttendance_WithNoAttendance_ShouldReturnEmpty() {
        when(attendanceRepository.findTodayAttendance(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Optional.empty());

        Optional<AttendanceDto> result = attendanceService.getTodayAttendance(1L);

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
    void hasCompletedAttendance_WithCompletedAttendance_ShouldReturnTrue() {
        existingAttendance.setClockOutTime(LocalDateTime.now().minusHours(4));
        LocalDateTime testDate = LocalDateTime.now();

        when(attendanceRepository.existsByEmployeeIdAndClockInTimeBetweenAndClockOutTimeIsNotNull(
                eq(1L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(true);

        boolean result = attendanceService.hasCompletedAttendance(1L, testDate);

        assertTrue(result);

        verify(attendanceRepository).existsByEmployeeIdAndClockInTimeBetweenAndClockOutTimeIsNotNull(
                eq(1L), any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    void hasCompletedAttendance_WithOnlyClockIn_ShouldReturnFalse() {
        LocalDateTime testDate = LocalDateTime.now();

        when(attendanceRepository.existsByEmployeeIdAndClockInTimeBetweenAndClockOutTimeIsNotNull(
                eq(1L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(false);

        boolean result = attendanceService.hasCompletedAttendance(1L, testDate);

        assertFalse(result);

        verify(attendanceRepository).existsByEmployeeIdAndClockInTimeBetweenAndClockOutTimeIsNotNull(
                eq(1L), any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    void hasCompletedAttendanceToday_WithCompletedAttendance_ShouldReturnTrue() {
        when(attendanceRepository.existsByEmployeeIdAndClockInTimeBetweenAndClockOutTimeIsNotNull(
                eq(1L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(true);

        boolean result = attendanceService.hasCompletedAttendanceToday(1L);

        assertTrue(result);

        verify(attendanceRepository).existsByEmployeeIdAndClockInTimeBetweenAndClockOutTimeIsNotNull(
                eq(1L), any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    void isClockedIn_WithNoActiveAttendance_ShouldReturnFalse() {
        when(attendanceRepository.existsByEmployeeIdAndClockOutTimeIsNull(1L))
                .thenReturn(false);

        boolean result = attendanceService.isClockedIn(1L);

        assertFalse(result);

        verify(attendanceRepository).existsByEmployeeIdAndClockOutTimeIsNull(1L);
    }

    @Test
    void getEmployeeAttendanceHistoryPaginated_ShouldReturnPageOfAttendance() {
        List<Attendance> attendances = Arrays.asList(existingAttendance);
        Page<Attendance> attendancePage = new PageImpl<>(attendances, PageRequest.of(0, 1), 1);

        when(attendanceRepository.findByEmployeeIdAndClockInTimeBetweenOrderByCreatedAtDesc(
                anyLong(), any(LocalDateTime.class), any(LocalDateTime.class), any(PageRequest.class)))
                .thenReturn(attendancePage);

        Page<AttendanceDto> result = attendanceService.getEmployeeAttendanceHistoryPaginated(1L, 7, 0, 1);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(0, result.getNumber());
        assertEquals(1, result.getSize());
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getTotalPages());

        verify(attendanceRepository).findByEmployeeIdAndClockInTimeBetweenOrderByCreatedAtDesc(
                eq(1L), any(LocalDateTime.class), any(LocalDateTime.class), any(PageRequest.class));
    }

    @Test
    void getEmployeeAttendanceHistoryPaginated_WithEmptyPage_ShouldReturnEmptyPage() {
        Page<Attendance> emptyPage = new PageImpl<>(Arrays.asList(), PageRequest.of(0, 1), 0);

        when(attendanceRepository.findByEmployeeIdAndClockInTimeBetweenOrderByCreatedAtDesc(
                anyLong(), any(LocalDateTime.class), any(LocalDateTime.class), any(PageRequest.class)))
                .thenReturn(emptyPage);

        Page<AttendanceDto> result = attendanceService.getEmployeeAttendanceHistoryPaginated(1L, 7, 0, 1);

        assertNotNull(result);
        assertEquals(0, result.getContent().size());
        assertEquals(0, result.getTotalElements());

        verify(attendanceRepository).findByEmployeeIdAndClockInTimeBetweenOrderByCreatedAtDesc(
                eq(1L), any(LocalDateTime.class), any(LocalDateTime.class), any(PageRequest.class));
    }

    @Test
    void testPaginatedAttendanceResponseStructure() {
        List<Attendance> attendances = Arrays.asList(existingAttendance);
        Page<Attendance> attendancePage = new PageImpl<>(attendances, PageRequest.of(0, 1), 1);

        when(attendanceRepository.findByEmployeeIdAndClockInTimeBetweenOrderByCreatedAtDesc(
                anyLong(), any(LocalDateTime.class), any(LocalDateTime.class), any(PageRequest.class)))
                .thenReturn(attendancePage);

        Page<AttendanceDto> result = attendanceService.getEmployeeAttendanceHistoryPaginated(1L, 7, 0, 1);

        // Test the response structure
        PaginatedAttendanceResponse response = PaginatedAttendanceResponse.createResponse(
            result.getContent(), result.getSize(),
            result.getTotalElements(), result.getTotalPages(), result.getNumber() + 1
        );

        assertEquals(1, response.getData().size());
        assertEquals(1, response.getPage().getSize());
        assertEquals(1, response.getPage().getTotal());
        assertEquals(1, response.getPage().getTotalPages());
        assertEquals(1, response.getPage().getCurrent());
    }

    @Test
    void testEmptyDataResponseFormat() {
        Page<Attendance> emptyPage = new PageImpl<>(Arrays.asList(), PageRequest.of(0, 10), 0);

        when(attendanceRepository.findByEmployeeIdAndClockInTimeBetweenOrderByCreatedAtDesc(
                anyLong(), any(LocalDateTime.class), any(LocalDateTime.class), any(PageRequest.class)))
                .thenReturn(emptyPage);

        Page<AttendanceDto> result = attendanceService.getEmployeeAttendanceHistoryPaginated(1L, 7, 0, 10);

        // Test the response structure for empty data
        PaginatedAttendanceResponse response = PaginatedAttendanceResponse.createResponse(
            result.getContent(), result.getSize(),
            result.getTotalElements(), result.getTotalPages(), result.getNumber() + 1
        );

        assertEquals(0, response.getData().size()); // Empty array
        assertEquals(10, response.getPage().getSize());
        assertEquals(0, response.getPage().getTotal());
        assertEquals(0, response.getPage().getTotalPages());
        assertEquals(1, response.getPage().getCurrent());
    }
}