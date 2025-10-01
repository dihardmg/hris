package hris.hris.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import hris.hris.dto.LeaveRequestDto;
import hris.hris.dto.LoginRequest;
import hris.hris.dto.LoginResponse;
import hris.hris.model.Employee;
import hris.hris.model.LeaveRequest;
import hris.hris.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
class LeaveRequestControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private Employee testEmployee;
    private Employee supervisorEmployee;
    private String employeeAuthToken;
    private String supervisorAuthToken;

    @BeforeEach
    void setUp() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        objectMapper = new ObjectMapper();

        supervisorEmployee = new Employee();
        supervisorEmployee.setFirstName("Supervisor");
        supervisorEmployee.setLastName("User");
        supervisorEmployee.setEmail("supervisor@example.com");
        supervisorEmployee.setPassword(passwordEncoder.encode("password123"));
        supervisorEmployee.setEmployeeId("SUP001");
        supervisorEmployee.setIsActive(true);
        supervisorEmployee.setAnnualLeaveBalance(15);
        supervisorEmployee.setSickLeaveBalance(12);
        employeeRepository.save(supervisorEmployee);

        testEmployee = new Employee();
        testEmployee.setFirstName("Test");
        testEmployee.setLastName("User");
        testEmployee.setEmail("test@example.com");
        testEmployee.setPassword(passwordEncoder.encode("password123"));
        testEmployee.setEmployeeId("EMP001");
        testEmployee.setSupervisorId(supervisorEmployee.getId());
        testEmployee.setIsActive(true);
        testEmployee.setAnnualLeaveBalance(12);
        testEmployee.setSickLeaveBalance(10);
        employeeRepository.save(testEmployee);

        employeeAuthToken = obtainAuthToken("test@example.com");
        supervisorAuthToken = obtainAuthToken("supervisor@example.com");
    }

    private String obtainAuthToken(String email) throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail(email);
        loginRequest.setPassword("password123");

        String response = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andReturn().getResponse().getContentAsString();

        LoginResponse loginResponse = objectMapper.readValue(response, LoginResponse.class);
        return "Bearer " + loginResponse.getToken();
    }

    @Test
    void createLeaveRequest_WithValidData_ShouldReturnSuccess() throws Exception {
        LeaveRequestDto request = new LeaveRequestDto();
        request.setLeaveType(LeaveRequest.LeaveType.ANNUAL_LEAVE);
        request.setStartDate(LocalDate.now().plusDays(1));
        request.setEndDate(LocalDate.now().plusDays(3));
        request.setTotalDays(3);
        request.setReason("Family vacation");

        mockMvc.perform(post("/api/leave/request")
                .header("Authorization", employeeAuthToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Leave request submitted successfully"))
                .andExpect(jsonPath("$.leaveRequest").exists())
                .andExpect(jsonPath("$.leaveRequest.leaveType").value("ANNUAL_LEAVE"))
                .andExpect(jsonPath("$.leaveRequest.status").value("PENDING"));
    }

    @Test
    void createLeaveRequest_WithInvalidDateRange_ShouldReturnBadRequest() throws Exception {
        LeaveRequestDto request = new LeaveRequestDto();
        request.setLeaveType(LeaveRequest.LeaveType.ANNUAL_LEAVE);
        request.setStartDate(LocalDate.now().plusDays(3));
        request.setEndDate(LocalDate.now().plusDays(1));
        request.setTotalDays(-2);
        request.setReason("Invalid date range");

        mockMvc.perform(post("/api/leave/request")
                .header("Authorization", employeeAuthToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void createLeaveRequest_WithPastDate_ShouldReturnBadRequest() throws Exception {
        LeaveRequestDto request = new LeaveRequestDto();
        request.setLeaveType(LeaveRequest.LeaveType.ANNUAL_LEAVE);
        request.setStartDate(LocalDate.now().minusDays(1));
        request.setEndDate(LocalDate.now().plusDays(1));
        request.setReason("Past date request");

        mockMvc.perform(post("/api/leave/request")
                .header("Authorization", employeeAuthToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void createLeaveRequest_WithInsufficientBalance_ShouldReturnBadRequest() throws Exception {
        LeaveRequestDto request = new LeaveRequestDto();
        request.setLeaveType(LeaveRequest.LeaveType.ANNUAL_LEAVE);
        request.setStartDate(LocalDate.now().plusDays(1));
        request.setEndDate(LocalDate.now().plusDays(30));
        request.setReason("Long leave request");

        mockMvc.perform(post("/api/leave/request")
                .header("Authorization", employeeAuthToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void createLeaveRequest_WithInvalidToken_ShouldReturnBadRequest() throws Exception {
        LeaveRequestDto request = new LeaveRequestDto();
        request.setLeaveType(LeaveRequest.LeaveType.ANNUAL_LEAVE);
        request.setStartDate(LocalDate.now().plusDays(1));
        request.setEndDate(LocalDate.now().plusDays(3));
        request.setReason("Invalid token request");

        mockMvc.perform(post("/api/leave/request")
                .header("Authorization", "Bearer invalid.token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createLeaveRequest_WithMissingToken_ShouldReturnBadRequest() throws Exception {
        LeaveRequestDto request = new LeaveRequestDto();
        request.setLeaveType(LeaveRequest.LeaveType.ANNUAL_LEAVE);
        request.setStartDate(LocalDate.now().plusDays(1));
        request.setEndDate(LocalDate.now().plusDays(3));
        request.setReason("No token request");

        mockMvc.perform(post("/api/leave/request")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getMyLeaveRequests_WithValidToken_ShouldReturnRequests() throws Exception {
        LeaveRequestDto request = new LeaveRequestDto();
        request.setLeaveType(LeaveRequest.LeaveType.ANNUAL_LEAVE);
        request.setStartDate(LocalDate.now().plusDays(1));
        request.setEndDate(LocalDate.now().plusDays(3));
        request.setReason("Test leave");

        mockMvc.perform(post("/api/leave/request")
                .header("Authorization", employeeAuthToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/leave/my-requests")
                .header("Authorization", employeeAuthToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].leaveType").value("ANNUAL"));
    }

    @Test
    void getMyLeaveRequests_WithNoRequests_ShouldReturnEmptyArray() throws Exception {
        mockMvc.perform(get("/api/leave/my-requests")
                .header("Authorization", employeeAuthToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getCurrentLeave_WithActiveLeave_ShouldReturnCurrentLeave() throws Exception {
        LeaveRequestDto request = new LeaveRequestDto();
        request.setLeaveType(LeaveRequest.LeaveType.ANNUAL_LEAVE);
        request.setStartDate(LocalDate.now().minusDays(1));
        request.setEndDate(LocalDate.now().plusDays(1));
        request.setReason("Current leave");

        mockMvc.perform(post("/api/leave/request")
                .header("Authorization", employeeAuthToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/leave/current")
                .header("Authorization", employeeAuthToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.leaveType").value("ANNUAL"));
    }

    @Test
    void getCurrentLeave_WithNoCurrentLeave_ShouldReturnMessage() throws Exception {
        mockMvc.perform(get("/api/leave/current")
                .header("Authorization", employeeAuthToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("No current leave found"));
    }

    @Test
    void getPendingLeaveRequests_WithSupervisorRole_ShouldReturnPendingRequests() throws Exception {
        LeaveRequestDto request = new LeaveRequestDto();
        request.setLeaveType(LeaveRequest.LeaveType.ANNUAL_LEAVE);
        request.setStartDate(LocalDate.now().plusDays(1));
        request.setEndDate(LocalDate.now().plusDays(3));
        request.setReason("Pending leave");

        mockMvc.perform(post("/api/leave/request")
                .header("Authorization", employeeAuthToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/leave/supervisor/pending")
                .header("Authorization", supervisorAuthToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].status").value("PENDING"));
    }

    @Test
    void approveLeaveRequest_WithValidApproval_ShouldReturnSuccess() throws Exception {
        LeaveRequestDto request = new LeaveRequestDto();
        request.setLeaveType(LeaveRequest.LeaveType.ANNUAL_LEAVE);
        request.setStartDate(LocalDate.now().plusDays(1));
        request.setEndDate(LocalDate.now().plusDays(3));
        request.setReason("Leave to approve");

        String createResponse = mockMvc.perform(post("/api/leave/request")
                .header("Authorization", employeeAuthToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andReturn().getResponse().getContentAsString();

        Map<String, Object> createResult = objectMapper.readValue(createResponse, Map.class);
        Map<String, Object> leaveRequest = (Map<String, Object>) createResult.get("leaveRequest");
        Integer requestId = (Integer) leaveRequest.get("id");

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("notes", "Approved by supervisor");

        mockMvc.perform(post("/api/leave/supervisor/approve/" + requestId)
                .header("Authorization", supervisorAuthToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Leave request approved successfully"))
                .andExpect(jsonPath("$.leaveRequest.status").value("APPROVED"));
    }

    @Test
    void approveLeaveRequest_WithNonExistentRequest_ShouldReturnBadRequest() throws Exception {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("notes", "Approve non-existent request");

        mockMvc.perform(post("/api/leave/supervisor/approve/99999")
                .header("Authorization", supervisorAuthToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void rejectLeaveRequest_WithValidRejection_ShouldReturnSuccess() throws Exception {
        LeaveRequestDto request = new LeaveRequestDto();
        request.setLeaveType(LeaveRequest.LeaveType.ANNUAL_LEAVE);
        request.setStartDate(LocalDate.now().plusDays(1));
        request.setEndDate(LocalDate.now().plusDays(3));
        request.setReason("Leave to reject");

        String createResponse = mockMvc.perform(post("/api/leave/request")
                .header("Authorization", employeeAuthToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andReturn().getResponse().getContentAsString();

        Map<String, Object> createResult = objectMapper.readValue(createResponse, Map.class);
        Map<String, Object> leaveRequest = (Map<String, Object>) createResult.get("leaveRequest");
        Integer requestId = (Integer) leaveRequest.get("id");

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("rejectionReason", "Insufficient staff coverage");

        mockMvc.perform(post("/api/leave/supervisor/reject/" + requestId)
                .header("Authorization", supervisorAuthToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Leave request rejected successfully"))
                .andExpect(jsonPath("$.leaveRequest.status").value("REJECTED"));
    }

    @Test
    void rejectLeaveRequest_WithMissingRejectionReason_ShouldReturnBadRequest() throws Exception {
        LeaveRequestDto request = new LeaveRequestDto();
        request.setLeaveType(LeaveRequest.LeaveType.ANNUAL_LEAVE);
        request.setStartDate(LocalDate.now().plusDays(1));
        request.setEndDate(LocalDate.now().plusDays(3));
        request.setReason("Leave to reject");

        String createResponse = mockMvc.perform(post("/api/leave/request")
                .header("Authorization", employeeAuthToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andReturn().getResponse().getContentAsString();

        Map<String, Object> createResult = objectMapper.readValue(createResponse, Map.class);
        Map<String, Object> leaveRequest = (Map<String, Object>) createResult.get("leaveRequest");
        Integer requestId = (Integer) leaveRequest.get("id");

        Map<String, String> requestBody = new HashMap<>();

        mockMvc.perform(post("/api/leave/supervisor/reject/" + requestId)
                .header("Authorization", supervisorAuthToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Rejection reason is required"));
    }

    @Test
    void rejectLeaveRequest_WithEmptyRejectionReason_ShouldReturnBadRequest() throws Exception {
        LeaveRequestDto request = new LeaveRequestDto();
        request.setLeaveType(LeaveRequest.LeaveType.ANNUAL_LEAVE);
        request.setStartDate(LocalDate.now().plusDays(1));
        request.setEndDate(LocalDate.now().plusDays(3));
        request.setReason("Leave to reject");

        String createResponse = mockMvc.perform(post("/api/leave/request")
                .header("Authorization", employeeAuthToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andReturn().getResponse().getContentAsString();

        Map<String, Object> createResult = objectMapper.readValue(createResponse, Map.class);
        Map<String, Object> leaveRequest = (Map<String, Object>) createResult.get("leaveRequest");
        Integer requestId = (Integer) leaveRequest.get("id");

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("rejectionReason", "");

        mockMvc.perform(post("/api/leave/supervisor/reject/" + requestId)
                .header("Authorization", supervisorAuthToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Rejection reason is required"));
    }

    @Test
    void getLeaveBalance_ShouldReturnMessage() throws Exception {
        mockMvc.perform(get("/api/leave/balance")
                .header("Authorization", employeeAuthToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Leave balance endpoint - implement as needed"));
    }

    @Test
    void createLeaveRequest_WithSickLeave_ShouldReturnSuccess() throws Exception {
        LeaveRequestDto request = new LeaveRequestDto();
        request.setLeaveType(LeaveRequest.LeaveType.SICK_LEAVE);
        request.setStartDate(LocalDate.now().plusDays(1));
        request.setEndDate(LocalDate.now().plusDays(2));
        request.setTotalDays(2);
        request.setReason("Sick leave");

        mockMvc.perform(post("/api/leave/request")
                .header("Authorization", employeeAuthToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.leaveRequest.leaveType").value("SICK"));
    }

    @Test
    void createLeaveRequest_WithMaternityLeave_ShouldReturnSuccess() throws Exception {
        LeaveRequestDto request = new LeaveRequestDto();
        request.setLeaveType(LeaveRequest.LeaveType.MATERNITY_LEAVE);
        request.setStartDate(LocalDate.now().plusDays(1));
        request.setEndDate(LocalDate.now().plusDays(90));
        request.setTotalDays(90);
        request.setReason("Maternity leave");

        mockMvc.perform(post("/api/leave/request")
                .header("Authorization", employeeAuthToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.leaveRequest.leaveType").value("MATERNITY"));
    }

    @Test
    void createLeaveRequest_WithInvalidJson_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/api/leave/request")
                .header("Authorization", employeeAuthToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{invalid json}"))
                .andExpect(status().isBadRequest());
    }
}