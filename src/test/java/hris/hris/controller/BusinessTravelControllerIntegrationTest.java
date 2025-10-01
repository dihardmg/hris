package hris.hris.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import hris.hris.dto.BusinessTravelRequestDto;
import hris.hris.dto.LoginRequest;
import hris.hris.dto.LoginResponse;
import hris.hris.model.Employee;
import hris.hris.model.BusinessTravelRequest;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
class BusinessTravelControllerIntegrationTest {

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
    void createBusinessTravelRequest_WithValidData_ShouldReturnSuccess() throws Exception {
        BusinessTravelRequestDto request = new BusinessTravelRequestDto();
        request.setDestination("Jakarta, Indonesia");
        request.setStartDate(LocalDate.now().plusDays(7));
        request.setEndDate(LocalDate.now().plusDays(10));
        request.setTravelPurpose("Client meeting and project discussion");
        request.setTransportationType(BusinessTravelRequest.TransportationType.FLIGHT);
        request.setAccommodationRequired(true);
        request.setEstimatedCost(new BigDecimal("5000.00"));

        mockMvc.perform(post("/api/business-travel/request")
                .header("Authorization", employeeAuthToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Business travel request submitted successfully"))
                .andExpect(jsonPath("$.travelRequest").exists())
                .andExpect(jsonPath("$.travelRequest.destination").value("Jakarta, Indonesia"))
                .andExpect(jsonPath("$.travelRequest.status").value("PENDING"));
    }

    @Test
    void createBusinessTravelRequest_WithInvalidDateRange_ShouldReturnBadRequest() throws Exception {
        BusinessTravelRequestDto request = new BusinessTravelRequestDto();
        request.setDestination("Jakarta, Indonesia");
        request.setStartDate(LocalDate.now().plusDays(10));
        request.setEndDate(LocalDate.now().plusDays(7));
        request.setTravelPurpose("Invalid date range");
        request.setTransportationType(BusinessTravelRequest.TransportationType.FLIGHT);
        request.setEstimatedCost(new BigDecimal("5000.00"));

        mockMvc.perform(post("/api/business-travel/request")
                .header("Authorization", employeeAuthToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void createBusinessTravelRequest_WithPastDate_ShouldReturnBadRequest() throws Exception {
        BusinessTravelRequestDto request = new BusinessTravelRequestDto();
        request.setDestination("Jakarta, Indonesia");
        request.setStartDate(LocalDate.now().minusDays(1));
        request.setEndDate(LocalDate.now().plusDays(1));
        request.setTravelPurpose("Past date request");
        request.setTransportationType(BusinessTravelRequest.TransportationType.FLIGHT);
        request.setEstimatedCost(new BigDecimal("5000.00"));

        mockMvc.perform(post("/api/business-travel/request")
                .header("Authorization", employeeAuthToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void createBusinessTravelRequest_WithMissingDestination_ShouldReturnBadRequest() throws Exception {
        BusinessTravelRequestDto request = new BusinessTravelRequestDto();
        request.setStartDate(LocalDate.now().plusDays(7));
        request.setEndDate(LocalDate.now().plusDays(10));
        request.setTravelPurpose("Missing destination");
        request.setTransportationType(BusinessTravelRequest.TransportationType.FLIGHT);
        request.setEstimatedCost(new BigDecimal("5000.00"));

        mockMvc.perform(post("/api/business-travel/request")
                .header("Authorization", employeeAuthToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createBusinessTravelRequest_WithNegativeCost_ShouldReturnBadRequest() throws Exception {
        BusinessTravelRequestDto request = new BusinessTravelRequestDto();
        request.setDestination("Jakarta, Indonesia");
        request.setStartDate(LocalDate.now().plusDays(7));
        request.setEndDate(LocalDate.now().plusDays(10));
        request.setTravelPurpose("Negative cost");
        request.setTransportationType(BusinessTravelRequest.TransportationType.FLIGHT);
        request.setEstimatedCost(new BigDecimal("-1000.00"));

        mockMvc.perform(post("/api/business-travel/request")
                .header("Authorization", employeeAuthToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createBusinessTravelRequest_WithInvalidToken_ShouldReturnBadRequest() throws Exception {
        BusinessTravelRequestDto request = new BusinessTravelRequestDto();
        request.setDestination("Jakarta, Indonesia");
        request.setStartDate(LocalDate.now().plusDays(7));
        request.setEndDate(LocalDate.now().plusDays(10));
        request.setTravelPurpose("Invalid token request");
        request.setTransportationType(BusinessTravelRequest.TransportationType.FLIGHT);
        request.setEstimatedCost(new BigDecimal("5000.00"));

        mockMvc.perform(post("/api/business-travel/request")
                .header("Authorization", "Bearer invalid.token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createBusinessTravelRequest_WithMissingToken_ShouldReturnBadRequest() throws Exception {
        BusinessTravelRequestDto request = new BusinessTravelRequestDto();
        request.setDestination("Jakarta, Indonesia");
        request.setStartDate(LocalDate.now().plusDays(7));
        request.setEndDate(LocalDate.now().plusDays(10));
        request.setTravelPurpose("No token request");
        request.setTransportationType(BusinessTravelRequest.TransportationType.FLIGHT);
        request.setEstimatedCost(new BigDecimal("5000.00"));

        mockMvc.perform(post("/api/business-travel/request")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getMyBusinessTravelRequests_WithValidToken_ShouldReturnRequests() throws Exception {
        BusinessTravelRequestDto request = new BusinessTravelRequestDto();
        request.setDestination("Jakarta, Indonesia");
        request.setStartDate(LocalDate.now().plusDays(7));
        request.setEndDate(LocalDate.now().plusDays(10));
        request.setTravelPurpose("Test travel");
        request.setTransportationType(BusinessTravelRequest.TransportationType.FLIGHT);
        request.setEstimatedCost(new BigDecimal("5000.00"));

        mockMvc.perform(post("/api/business-travel/request")
                .header("Authorization", employeeAuthToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/business-travel/my-requests")
                .header("Authorization", employeeAuthToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].destination").value("Jakarta, Indonesia"));
    }

    @Test
    void getMyBusinessTravelRequests_WithNoRequests_ShouldReturnEmptyArray() throws Exception {
        mockMvc.perform(get("/api/business-travel/my-requests")
                .header("Authorization", employeeAuthToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getCurrentTravel_WithActiveTravel_ShouldReturnCurrentTravel() throws Exception {
        BusinessTravelRequestDto request = new BusinessTravelRequestDto();
        request.setDestination("Jakarta, Indonesia");
        request.setStartDate(LocalDate.now().minusDays(1));
        request.setEndDate(LocalDate.now().plusDays(1));
        request.setTravelPurpose("Current travel");
        request.setTransportationType(BusinessTravelRequest.TransportationType.FLIGHT);
        request.setEstimatedCost(new BigDecimal("5000.00"));

        mockMvc.perform(post("/api/business-travel/request")
                .header("Authorization", employeeAuthToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/business-travel/current")
                .header("Authorization", employeeAuthToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].destination").value("Jakarta, Indonesia"));
    }

    @Test
    void getCurrentTravel_WithNoCurrentTravel_ShouldReturnMessage() throws Exception {
        mockMvc.perform(get("/api/business-travel/current")
                .header("Authorization", employeeAuthToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("No current business travel found"));
    }

    @Test
    void getPendingBusinessTravelRequests_WithSupervisorRole_ShouldReturnPendingRequests() throws Exception {
        BusinessTravelRequestDto request = new BusinessTravelRequestDto();
        request.setDestination("Jakarta, Indonesia");
        request.setStartDate(LocalDate.now().plusDays(7));
        request.setEndDate(LocalDate.now().plusDays(10));
        request.setTravelPurpose("Pending travel");
        request.setTransportationType(BusinessTravelRequest.TransportationType.FLIGHT);
        request.setEstimatedCost(new BigDecimal("5000.00"));

        mockMvc.perform(post("/api/business-travel/request")
                .header("Authorization", employeeAuthToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/business-travel/supervisor/pending")
                .header("Authorization", supervisorAuthToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].status").value("PENDING"));
    }

    @Test
    void approveBusinessTravelRequest_WithValidApproval_ShouldReturnSuccess() throws Exception {
        BusinessTravelRequestDto request = new BusinessTravelRequestDto();
        request.setDestination("Jakarta, Indonesia");
        request.setStartDate(LocalDate.now().plusDays(7));
        request.setEndDate(LocalDate.now().plusDays(10));
        request.setTravelPurpose("Travel to approve");
        request.setTransportationType(BusinessTravelRequest.TransportationType.FLIGHT);
        request.setEstimatedCost(new BigDecimal("5000.00"));

        String createResponse = mockMvc.perform(post("/api/business-travel/request")
                .header("Authorization", employeeAuthToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andReturn().getResponse().getContentAsString();

        Map<String, Object> createResult = objectMapper.readValue(createResponse, Map.class);
        Map<String, Object> travelRequest = (Map<String, Object>) createResult.get("travelRequest");
        Integer requestId = (Integer) travelRequest.get("id");

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("notes", "Approved by supervisor");

        mockMvc.perform(post("/api/business-travel/supervisor/approve/" + requestId)
                .header("Authorization", supervisorAuthToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Business travel request approved successfully"))
                .andExpect(jsonPath("$.travelRequest.status").value("APPROVED"));
    }

    @Test
    void approveBusinessTravelRequest_WithNonExistentRequest_ShouldReturnBadRequest() throws Exception {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("notes", "Approve non-existent request");

        mockMvc.perform(post("/api/business-travel/supervisor/approve/99999")
                .header("Authorization", supervisorAuthToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void rejectBusinessTravelRequest_WithValidRejection_ShouldReturnSuccess() throws Exception {
        BusinessTravelRequestDto request = new BusinessTravelRequestDto();
        request.setDestination("Jakarta, Indonesia");
        request.setStartDate(LocalDate.now().plusDays(7));
        request.setEndDate(LocalDate.now().plusDays(10));
        request.setTravelPurpose("Travel to reject");
        request.setTransportationType(BusinessTravelRequest.TransportationType.FLIGHT);
        request.setEstimatedCost(new BigDecimal("5000.00"));

        String createResponse = mockMvc.perform(post("/api/business-travel/request")
                .header("Authorization", employeeAuthToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andReturn().getResponse().getContentAsString();

        Map<String, Object> createResult = objectMapper.readValue(createResponse, Map.class);
        Map<String, Object> travelRequest = (Map<String, Object>) createResult.get("travelRequest");
        Integer requestId = (Integer) travelRequest.get("id");

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("rejectionReason", "Budget constraints");

        mockMvc.perform(post("/api/business-travel/supervisor/reject/" + requestId)
                .header("Authorization", supervisorAuthToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Business travel request rejected successfully"))
                .andExpect(jsonPath("$.travelRequest.status").value("REJECTED"));
    }

    @Test
    void rejectBusinessTravelRequest_WithMissingRejectionReason_ShouldReturnBadRequest() throws Exception {
        BusinessTravelRequestDto request = new BusinessTravelRequestDto();
        request.setDestination("Jakarta, Indonesia");
        request.setStartDate(LocalDate.now().plusDays(7));
        request.setEndDate(LocalDate.now().plusDays(10));
        request.setTravelPurpose("Travel to reject");
        request.setTransportationType(BusinessTravelRequest.TransportationType.FLIGHT);
        request.setEstimatedCost(new BigDecimal("5000.00"));

        String createResponse = mockMvc.perform(post("/api/business-travel/request")
                .header("Authorization", employeeAuthToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andReturn().getResponse().getContentAsString();

        Map<String, Object> createResult = objectMapper.readValue(createResponse, Map.class);
        Map<String, Object> travelRequest = (Map<String, Object>) createResult.get("travelRequest");
        Integer requestId = (Integer) travelRequest.get("id");

        Map<String, String> requestBody = new HashMap<>();

        mockMvc.perform(post("/api/business-travel/supervisor/reject/" + requestId)
                .header("Authorization", supervisorAuthToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Rejection reason is required"));
    }

    @Test
    void rejectBusinessTravelRequest_WithEmptyRejectionReason_ShouldReturnBadRequest() throws Exception {
        BusinessTravelRequestDto request = new BusinessTravelRequestDto();
        request.setDestination("Jakarta, Indonesia");
        request.setStartDate(LocalDate.now().plusDays(7));
        request.setEndDate(LocalDate.now().plusDays(10));
        request.setTravelPurpose("Travel to reject");
        request.setTransportationType(BusinessTravelRequest.TransportationType.FLIGHT);
        request.setEstimatedCost(new BigDecimal("5000.00"));

        String createResponse = mockMvc.perform(post("/api/business-travel/request")
                .header("Authorization", employeeAuthToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andReturn().getResponse().getContentAsString();

        Map<String, Object> createResult = objectMapper.readValue(createResponse, Map.class);
        Map<String, Object> travelRequest = (Map<String, Object>) createResult.get("travelRequest");
        Integer requestId = (Integer) travelRequest.get("id");

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("rejectionReason", "");

        mockMvc.perform(post("/api/business-travel/supervisor/reject/" + requestId)
                .header("Authorization", supervisorAuthToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Rejection reason is required"));
    }

    @Test
    void createBusinessTravelRequest_WithDifferentTransportationTypes_ShouldReturnSuccess() throws Exception {
        BusinessTravelRequest.TransportationType[] transportTypes = {
            BusinessTravelRequest.TransportationType.FLIGHT,
            BusinessTravelRequest.TransportationType.TRAIN,
            BusinessTravelRequest.TransportationType.BUS,
            BusinessTravelRequest.TransportationType.CAR,
            BusinessTravelRequest.TransportationType.OTHER
        };

        for (BusinessTravelRequest.TransportationType transportType : transportTypes) {
            BusinessTravelRequestDto request = new BusinessTravelRequestDto();
            request.setDestination("Test Destination");
            request.setStartDate(LocalDate.now().plusDays(7));
            request.setEndDate(LocalDate.now().plusDays(10));
            request.setTravelPurpose("Test with " + transportType);
            request.setTransportationType(transportType);
            request.setEstimatedCost(new BigDecimal("5000.00"));

            mockMvc.perform(post("/api/business-travel/request")
                    .header("Authorization", employeeAuthToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.travelRequest.transportationType").value(transportType));
        }
    }

    @Test
    void createBusinessTravelRequest_WithDifferentAccommodationTypes_ShouldReturnSuccess() throws Exception {
        Boolean[] accommodationTypes = {true, false};

        for (Boolean accommodationType : accommodationTypes) {
            BusinessTravelRequestDto request = new BusinessTravelRequestDto();
            request.setDestination("Test Destination");
            request.setStartDate(LocalDate.now().plusDays(7));
            request.setEndDate(LocalDate.now().plusDays(10));
            request.setTravelPurpose("Test with accommodation: " + accommodationType);
            request.setTransportationType(BusinessTravelRequest.TransportationType.FLIGHT);
            request.setAccommodationRequired(accommodationType);
            request.setEstimatedCost(new BigDecimal("5000.00"));

            mockMvc.perform(post("/api/business-travel/request")
                    .header("Authorization", employeeAuthToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.travelRequest.accommodationType").value(accommodationType));
        }
    }

    @Test
    void createBusinessTravelRequest_WithVeryHighCost_ShouldReturnSuccess() throws Exception {
        BusinessTravelRequestDto request = new BusinessTravelRequestDto();
        request.setDestination("International Destination");
        request.setStartDate(LocalDate.now().plusDays(14));
        request.setEndDate(LocalDate.now().plusDays(30));
        request.setTravelPurpose("International business trip");
        request.setTransportationType(BusinessTravelRequest.TransportationType.FLIGHT);
        request.setEstimatedCost(new BigDecimal("50000.00"));
        
        mockMvc.perform(post("/api/business-travel/request")
                .header("Authorization", employeeAuthToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.travelRequest.estimatedCost").value(50000.00));
    }

    @Test
    void createBusinessTravelRequest_WithInvalidJson_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/api/business-travel/request")
                .header("Authorization", employeeAuthToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{invalid json}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createBusinessTravelRequest_WithOneDayTrip_ShouldReturnSuccess() throws Exception {
        BusinessTravelRequestDto request = new BusinessTravelRequestDto();
        request.setDestination("Local City");
        request.setStartDate(LocalDate.now().plusDays(7));
        request.setEndDate(LocalDate.now().plusDays(7));
        request.setTravelPurpose("One day business meeting");
        request.setTransportationType(BusinessTravelRequest.TransportationType.CAR);
        request.setEstimatedCost(new BigDecimal("500.00"));

        mockMvc.perform(post("/api/business-travel/request")
                .header("Authorization", employeeAuthToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.travelRequest.destination").value("Local City"));
    }
}