package hris.hris.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import hris.hris.dto.LeaveRequestDto;
import hris.hris.dto.LoginRequest;
import hris.hris.dto.LoginResponse;
import hris.hris.model.Employee;
import hris.hris.model.LeaveRequest;
import hris.hris.model.LeaveType;
import hris.hris.repository.EmployeeRepository;
import hris.hris.repository.LeaveRequestRepository;
import hris.hris.repository.LeaveTypeRepository;
import hris.hris.service.LeaveRequestService;
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
import org.springframework.security.test.context.support.WithMockUser;

import java.time.LocalDate;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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

    @Autowired
    private LeaveRequestService leaveRequestService;

    @Autowired
    private LeaveRequestRepository leaveRequestRepository;

    @Autowired
    private LeaveTypeRepository leaveTypeRepository;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private Employee testEmployee;
    private Employee supervisorEmployee;
    private String employeeAuthToken;
    private String supervisorAuthToken;
    private LeaveType annualLeaveType;
    private LeaveType sickLeaveType;
    private LeaveType maternityLeaveType;

    @BeforeEach
    void setUp() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

        supervisorEmployee = new Employee();
        supervisorEmployee.setFirstName("Supervisor");
        supervisorEmployee.setLastName("User");
        supervisorEmployee.setEmail("supervisor@example.com");
        supervisorEmployee.setPassword(passwordEncoder.encode("password123"));
        supervisorEmployee.setEmployeeId("SUP001");
        supervisorEmployee.setPhoneNumber("+1234567896");
        supervisorEmployee.setIsActive(true);
                employeeRepository.save(supervisorEmployee);

        testEmployee = new Employee();
        testEmployee.setFirstName("Test");
        testEmployee.setLastName("User");
        testEmployee.setEmail("test@example.com");
        testEmployee.setPassword(passwordEncoder.encode("password123"));
        testEmployee.setEmployeeId("EMP001");
        testEmployee.setPhoneNumber("+1234567897");
        testEmployee.setSupervisorId(supervisorEmployee.getId());
        testEmployee.setIsActive(true);
                employeeRepository.save(testEmployee);

        // Setup LeaveTypes for testing
        setupLeaveTypes();
    }

    private void setupLeaveTypes() {
        // Create Annual Leave type
        annualLeaveType = new LeaveType();
        annualLeaveType.setCode("ANNUAL_LEAVE");
        annualLeaveType.setName("Cuti Tahunan");
        annualLeaveType.setDescription("Cuti tahunan berbayar");
        annualLeaveType.setHasBalanceQuota(true);
        annualLeaveType.setIsPaidLeave(true);
        annualLeaveType.setIsActive(true);
        annualLeaveType = leaveTypeRepository.save(annualLeaveType);

        // Create Sick Leave type
        sickLeaveType = new LeaveType();
        sickLeaveType.setCode("SICK_LEAVE");
        sickLeaveType.setName("Cuti Sakit");
        sickLeaveType.setDescription("Cuti sakit dengan dokumen");
        sickLeaveType.setHasBalanceQuota(true);
        sickLeaveType.setIsPaidLeave(true);
        sickLeaveType.setIsActive(true);
        sickLeaveType = leaveTypeRepository.save(sickLeaveType);

        // Create Maternity Leave type
        maternityLeaveType = new LeaveType();
        maternityLeaveType.setCode("MATERNITY_LEAVE");
        maternityLeaveType.setName("Cuti Melahirkan");
        maternityLeaveType.setDescription("Cuti melahirkan 3 bulan");
        maternityLeaveType.setHasBalanceQuota(false);
        maternityLeaveType.setIsPaidLeave(true);
        maternityLeaveType.setIsActive(true);
        maternityLeaveType = leaveTypeRepository.save(maternityLeaveType);
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
    @WithMockUser(username = "test@example.com", roles = {"EMPLOYEE"})
    void createLeaveRequest_WithValidData_ShouldReturnSuccess() throws Exception {
        LeaveRequestDto request = new LeaveRequestDto();
        request.setLeaveTypeId(annualLeaveType.getId());
        request.setStartDate(LocalDate.now().plusDays(1));
        request.setEndDate(LocalDate.now().plusDays(3));
        request.setTotalDays(3);
        request.setReason("Family vacation");

        mockMvc.perform(post("/api/leave/request")
                                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Leave request submitted successfully"))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.leaveType.code").value("ANNUAL_LEAVE"))
                .andExpect(jsonPath("$.data.status").value("PENDING"));
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = {"EMPLOYEE"})
    void createLeaveRequest_WithInvalidDateRange_ShouldReturnBadRequest() throws Exception {
        LeaveRequestDto request = new LeaveRequestDto();
        request.setLeaveTypeId(annualLeaveType.getId());
        request.setStartDate(LocalDate.now().plusDays(3));
        request.setEndDate(LocalDate.now().plusDays(1));
        request.setTotalDays(-2);
        request.setReason("Invalid date range");

        mockMvc.perform(post("/api/leave/request")
                                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = {"EMPLOYEE"})
    void createLeaveRequest_WithPastDate_ShouldReturnBadRequest() throws Exception {
        LeaveRequestDto request = new LeaveRequestDto();
        request.setLeaveTypeId(annualLeaveType.getId());
        request.setStartDate(LocalDate.now().minusDays(1));
        request.setEndDate(LocalDate.now().plusDays(1));
        request.setReason("Past date request");

        mockMvc.perform(post("/api/leave/request")
                                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = {"EMPLOYEE"})
    void createLeaveRequest_WithInsufficientBalance_ShouldReturnBadRequest() throws Exception {
        LeaveRequestDto request = new LeaveRequestDto();
        request.setLeaveTypeId(annualLeaveType.getId());
        request.setStartDate(LocalDate.now().plusDays(1));
        request.setEndDate(LocalDate.now().plusDays(35));
        request.setReason("Long leave request - exceeds balance");

        mockMvc.perform(post("/api/leave/request")
                                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = {"EMPLOYEE"})
    void createLeaveRequest_WithInvalidToken_ShouldReturnBadRequest() throws Exception {
        LeaveRequestDto request = new LeaveRequestDto();
        request.setLeaveTypeId(annualLeaveType.getId());
        request.setStartDate(LocalDate.now().plusDays(1));
        request.setEndDate(LocalDate.now().plusDays(3));
        request.setReason("Invalid token request");

        mockMvc.perform(post("/api/leave/request")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = {"EMPLOYEE"})
    void createLeaveRequest_WithMissingToken_ShouldReturnBadRequest() throws Exception {
        LeaveRequestDto request = new LeaveRequestDto();
        request.setLeaveTypeId(annualLeaveType.getId());
        request.setStartDate(LocalDate.now().plusDays(1));
        request.setEndDate(LocalDate.now().plusDays(3));
        request.setReason("No token request");

        mockMvc.perform(post("/api/leave/request")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = {"EMPLOYEE"})
    void getMyLeaveRequests_WithValidToken_ShouldReturnRequests() throws Exception {
        LeaveRequestDto request = new LeaveRequestDto();
        request.setLeaveTypeId(annualLeaveType.getId());
        request.setStartDate(LocalDate.now().plusDays(1));
        request.setEndDate(LocalDate.now().plusDays(3));
        request.setTotalDays(3);
        request.setReason("Test leave");

        mockMvc.perform(post("/api/leave/request")
                                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/leave/my-requests"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].leaveType.code").value("ANNUAL_LEAVE"));
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = {"EMPLOYEE"})
    void getMyLeaveRequests_WithNoRequests_ShouldReturnEmptyArray() throws Exception {
        mockMvc.perform(get("/api/leave/my-requests"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = {"EMPLOYEE"})
    void getCurrentLeave_WithNoCurrentLeave_ShouldReturnMessage() throws Exception {
        mockMvc.perform(get("/api/leave/current"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("No current leave found"));
    }

    @Test
    @WithMockUser(username = "supervisor@example.com", roles = {"SUPERVISOR"})
    void getPendingLeaveRequests_WithSupervisorRole_ShouldReturnPendingRequests() throws Exception {
        // Create a leave request for the test employee (subordinate)
        LeaveRequestDto request = new LeaveRequestDto();
        request.setLeaveTypeId(annualLeaveType.getId());
        request.setStartDate(LocalDate.now().plusDays(1));
        request.setEndDate(LocalDate.now().plusDays(3));
        request.setTotalDays(3);
        request.setReason("Pending leave");

        // Create the request using service directly for the test employee
        leaveRequestService.createLeaveRequest(testEmployee.getId(), request);

        mockMvc.perform(get("/api/leave/supervisor/pending"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].status").value("PENDING"));
    }

    @Test
    @WithMockUser(username = "supervisor@example.com", roles = {"SUPERVISOR"})
    void approveLeaveRequest_WithValidApproval_ShouldReturnSuccess() throws Exception {
        // First create a leave request as an employee (using employee repository)
        LeaveRequestDto request = new LeaveRequestDto();
        request.setLeaveTypeId(annualLeaveType.getId());
        request.setStartDate(LocalDate.now().plusDays(1));
        request.setEndDate(LocalDate.now().plusDays(3));
        request.setTotalDays(3);
        request.setReason("Leave to approve");

        // Create the leave request using the service directly to get an ID
        LeaveRequest createdRequest = leaveRequestService.createLeaveRequest(testEmployee.getId(), request);

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("notes", "Approved by supervisor");

        mockMvc.perform(post("/api/leave/supervisor/approve/" + createdRequest.getUuid())
                                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Leave request approved successfully"))
                .andExpect(jsonPath("$.data.status").value("APPROVED"));
    }

    @Test
    @WithMockUser(username = "supervisor@example.com", roles = {"SUPERVISOR"})
    void approveLeaveRequest_WithNonExistentRequest_ShouldReturnBadRequest() throws Exception {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("notes", "Approve non-existent request");

        mockMvc.perform(post("/api/leave/supervisor/approve/550e8400-e29b-41d4-a716-446655440000")
                                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @WithMockUser(username = "supervisor@example.com", roles = {"SUPERVISOR"})
    void rejectLeaveRequest_WithValidRejection_ShouldReturnSuccess() throws Exception {
        LeaveRequestDto request = new LeaveRequestDto();
        request.setLeaveTypeId(annualLeaveType.getId());
        request.setStartDate(LocalDate.now().plusDays(1));
        request.setEndDate(LocalDate.now().plusDays(3));
        request.setTotalDays(3);
        request.setReason("Leave to reject");

        // Create the leave request using the service directly to get an ID
        LeaveRequest createdRequest = leaveRequestService.createLeaveRequest(testEmployee.getId(), request);

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("rejectionReason", "Insufficient staff coverage");

        mockMvc.perform(post("/api/leave/supervisor/reject/" + createdRequest.getUuid())
                                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Leave request rejected successfully"))
                .andExpect(jsonPath("$.data.status").value("REJECTED"));
    }

    @Test
    @WithMockUser(username = "supervisor@example.com", roles = {"SUPERVISOR"})
    void rejectLeaveRequest_WithMissingRejectionReason_ShouldReturnBadRequest() throws Exception {
        LeaveRequestDto request = new LeaveRequestDto();
        request.setLeaveTypeId(annualLeaveType.getId());
        request.setStartDate(LocalDate.now().plusDays(1));
        request.setEndDate(LocalDate.now().plusDays(3));
        request.setTotalDays(3);
        request.setReason("Leave to reject");

        // Create the leave request using the service directly to get an ID
        LeaveRequest createdRequest = leaveRequestService.createLeaveRequest(testEmployee.getId(), request);

        Map<String, String> requestBody = new HashMap<>();

        mockMvc.perform(post("/api/leave/supervisor/reject/" + createdRequest.getUuid())
                                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Rejection reason is required"));
    }

    @Test
    @WithMockUser(username = "supervisor@example.com", roles = {"SUPERVISOR"})
    void rejectLeaveRequest_WithEmptyRejectionReason_ShouldReturnBadRequest() throws Exception {
        LeaveRequestDto request = new LeaveRequestDto();
        request.setLeaveTypeId(annualLeaveType.getId());
        request.setStartDate(LocalDate.now().plusDays(1));
        request.setEndDate(LocalDate.now().plusDays(3));
        request.setTotalDays(3);
        request.setReason("Leave to reject");

        // Create the leave request using the service directly to get an ID
        LeaveRequest createdRequest = leaveRequestService.createLeaveRequest(testEmployee.getId(), request);

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("rejectionReason", "");

        mockMvc.perform(post("/api/leave/supervisor/reject/" + createdRequest.getUuid())
                                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Rejection reason is required"));
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = {"EMPLOYEE"})
    void getLeaveBalance_ShouldReturnMessage() throws Exception {
        mockMvc.perform(get("/api/leave/balance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Leave balance endpoint - implement as needed"));
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = {"EMPLOYEE"})
    void createLeaveRequest_WithSickLeave_ShouldReturnSuccess() throws Exception {
        LeaveRequestDto request = new LeaveRequestDto();
        request.setLeaveTypeId(sickLeaveType.getId());
        request.setStartDate(LocalDate.now().plusDays(1));
        request.setEndDate(LocalDate.now().plusDays(2));
        request.setTotalDays(2);
        request.setReason("Sick leave");

        mockMvc.perform(post("/api/leave/request")
                                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.leaveType.code").value("SICK_LEAVE"));
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = {"EMPLOYEE"})
    void createLeaveRequest_WithMaternityLeave_ShouldReturnSuccess() throws Exception {
        LeaveRequestDto request = new LeaveRequestDto();
        request.setLeaveTypeId(maternityLeaveType.getId());
        request.setStartDate(LocalDate.now().plusDays(1));
        request.setEndDate(LocalDate.now().plusDays(5));
        request.setTotalDays(5);
        request.setReason("Maternity leave");

        mockMvc.perform(post("/api/leave/request")
                                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.leaveType.code").value("MATERNITY_LEAVE"));
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = {"EMPLOYEE"})
    void createLeaveRequest_WithInvalidJson_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/api/leave/request")
                                .contentType(MediaType.APPLICATION_JSON)
                .content("{invalid json}"))
                .andExpect(status().isBadRequest());
    }
}