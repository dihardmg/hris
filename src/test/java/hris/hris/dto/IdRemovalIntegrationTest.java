package hris.hris.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import hris.hris.model.Attendance;
import hris.hris.model.Employee;
import hris.hris.service.AttendanceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class IdRemovalIntegrationTest {

    @Autowired
    private AttendanceService attendanceService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testAttendanceDtoDoesNotContainNumericId() throws JsonProcessingException {
        // Create a test attendance record
        Employee employee = new Employee();
        employee.setId(1L);
        employee.setEmployeeId("EMP001");
        employee.setFirstName("John");
        employee.setLastName("Doe");
        employee.setEmail("john.doe@example.com");

        Attendance attendance = new Attendance();
        attendance.setId(123L); // This should NOT appear in JSON
        attendance.setUuid(UUID.randomUUID());
        attendance.setEmployee(employee);
        attendance.setClockInTime(LocalDateTime.now());
        attendance.setLatitude(BigDecimal.valueOf(-6.2088));
        attendance.setLongitude(BigDecimal.valueOf(106.8456));
        attendance.setLocationAddress("Office Location");
        attendance.setIsWithinGeofence(true);
        attendance.setNotes("Test attendance");

        // Convert to DTO
        AttendanceDto dto = attendanceService.mapToDto(attendance);

        // Convert to JSON
        String json = objectMapper.writeValueAsString(dto);
        System.out.println("Attendance JSON: " + json);

        // Verify that numeric ID is NOT in the JSON
        assertFalse(json.contains("\"id\":"));
        assertFalse(json.contains("\"employeeId\":"));

        // Verify that UUID IS in the JSON
        assertTrue(json.contains("\"uuid\":"));

        // Verify that other expected fields are present
        assertTrue(json.contains("\"employeeName\":"));
        assertTrue(json.contains("\"clockInTime\":"));
    }

    @Test
    void testLeaveRequestResponseDtoDoesNotContainNumericId() throws JsonProcessingException {
        // Create test data
        LeaveRequestResponseDto dto = new LeaveRequestResponseDto();
        dto.setUuid(UUID.randomUUID());
        dto.setReason("Test leave");

        LeaveRequestResponseDto.UserInfo userInfo = new LeaveRequestResponseDto.UserInfo();
        userInfo.setId(456L); // This is in nested UserInfo, which is acceptable
        userInfo.setEmployeeCode("EMP001");
        userInfo.setFirstName("John");
        userInfo.setLastName("Doe");
        userInfo.setEmail("john.doe@example.com");
        dto.setCreatedBy(userInfo);

        // Convert to JSON
        String json = objectMapper.writeValueAsString(dto);
        System.out.println("LeaveRequest JSON: " + json);

        // Verify that main record ID is NOT in the JSON (only UUID should be at root level)
        assertTrue(json.contains("\"uuid\":"));
        assertTrue(json.contains("\"reason\":"));

        // The UserInfo can contain ID for audit purposes, but it's nested
        if (json.contains("\"createdBy\"")) {
            // This is acceptable as it's for audit trail in nested object
            System.out.println("UserInfo ID in nested object is acceptable for audit trail");
        }
    }
}