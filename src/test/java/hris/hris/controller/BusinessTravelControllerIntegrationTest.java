package hris.hris.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import hris.hris.dto.BusinessTravelRequestDto;
import hris.hris.dto.LoginRequest;
import hris.hris.dto.LoginResponse;
import hris.hris.model.Employee;
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
import java.util.UUID;

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
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

        supervisorEmployee = new Employee();
        supervisorEmployee.setFirstName("Supervisor");
        supervisorEmployee.setLastName("User");
        supervisorEmployee.setEmail("supervisor@example.com");
        supervisorEmployee.setPassword(passwordEncoder.encode("password123"));
        supervisorEmployee.setEmployeeId("BTSUP001");
        supervisorEmployee.setPhoneNumber("+1234567895");
        supervisorEmployee.setIsActive(true);
              employeeRepository.save(supervisorEmployee);

        testEmployee = new Employee();
        testEmployee.setFirstName("Test");
        testEmployee.setLastName("User");
        testEmployee.setEmail("test@example.com");
        testEmployee.setPassword(passwordEncoder.encode("password123"));
        testEmployee.setEmployeeId("BTEMP001");
        testEmployee.setPhoneNumber("+1234567894");
        testEmployee.setSupervisorId(supervisorEmployee.getId());
        testEmployee.setIsActive(true);
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
        request.setCityId(1L); // Assuming Jakarta city has ID 1
        request.setStartDate(LocalDate.now().plusDays(7));
        request.setEndDate(LocalDate.now().plusDays(10));
        request.setReason("Client meeting and project discussion");

        mockMvc.perform(post("/api/business-travel/request")
                .header("Authorization", employeeAuthToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Business travel request submitted successfully"))
                .andExpect(jsonPath("$.travelRequest.cityName").value("Jakarta"))
                .andExpect(jsonPath("$.travelRequest.totalDays").value(4));
    }

    @Test
    void createBusinessTravelRequest_WithInvalidDateRange_ShouldReturnBadRequest() throws Exception {
        BusinessTravelRequestDto request = new BusinessTravelRequestDto();
        request.setCityId(1L); // Assuming Jakarta city has ID 1
        request.setStartDate(LocalDate.now().plusDays(10));
        request.setEndDate(LocalDate.now().plusDays(7));
        request.setReason("Invalid date range");

        mockMvc.perform(post("/api/business-travel/request")
                .header("Authorization", employeeAuthToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createBusinessTravelRequest_WithPastDate_ShouldReturnBadRequest() throws Exception {
        BusinessTravelRequestDto request = new BusinessTravelRequestDto();
        request.setCityId(1L); // Assuming Jakarta city has ID 1
        request.setStartDate(LocalDate.now().minusDays(1));
        request.setEndDate(LocalDate.now().plusDays(1));
        request.setReason("Past date request");

        mockMvc.perform(post("/api/business-travel/request")
                .header("Authorization", employeeAuthToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createBusinessTravelRequest_WithMissingCity_ShouldReturnBadRequest() throws Exception {
        BusinessTravelRequestDto request = new BusinessTravelRequestDto();
        request.setStartDate(LocalDate.now().plusDays(7));
        request.setEndDate(LocalDate.now().plusDays(10));
        request.setReason("Missing city");

        mockMvc.perform(post("/api/business-travel/request")
                .header("Authorization", employeeAuthToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createBusinessTravelRequest_WithInvalidToken_ShouldReturnUnauthorized() throws Exception {
        BusinessTravelRequestDto request = new BusinessTravelRequestDto();
        request.setCityId(1L); // Assuming Jakarta city has ID 1
        request.setStartDate(LocalDate.now().plusDays(7));
        request.setEndDate(LocalDate.now().plusDays(10));
        request.setReason("Invalid token request");

        mockMvc.perform(post("/api/business-travel/request")
                .header("Authorization", "Bearer invalid.token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createBusinessTravelRequest_WithMissingToken_ShouldReturnUnauthorized() throws Exception {
        BusinessTravelRequestDto request = new BusinessTravelRequestDto();
        request.setCityId(1L); // Assuming Jakarta city has ID 1
        request.setStartDate(LocalDate.now().plusDays(7));
        request.setEndDate(LocalDate.now().plusDays(10));
        request.setReason("No token request");

        mockMvc.perform(post("/api/business-travel/request")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getMyBusinessTravelRequests_WithValidToken_ShouldReturnPaginatedRequests() throws Exception {
        // First create a travel request
        BusinessTravelRequestDto request = new BusinessTravelRequestDto();
        request.setCityId(1L); // Assuming Jakarta city has ID 1
        request.setStartDate(LocalDate.now().plusDays(7));
        request.setEndDate(LocalDate.now().plusDays(10));
        request.setReason("Test travel");

        mockMvc.perform(post("/api/business-travel/request")
                .header("Authorization", employeeAuthToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // Then get paginated requests
        mockMvc.perform(get("/api/business-travel/my-requests")
                .header("Authorization", employeeAuthToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.page.size").value(10))
                .andExpect(jsonPath("$.page.current").value(1));
    }

    @Test
    void getMyBusinessTravelRequests_WithPagination_ShouldReturnPaginatedResults() throws Exception {
        mockMvc.perform(get("/api/business-travel/my-requests")
                .param("page", "0")
                .param("size", "5")
                .header("Authorization", employeeAuthToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page.size").value(5))
                .andExpect(jsonPath("$.page.current").value(1));
    }

    @Test
    void getBusinessTravelRequestByUuid_WithValidUuid_ShouldReturnRequest() throws Exception {
        // First create a travel request
        var result = mockMvc.perform(post("/api/business-travel/request")
                .header("Authorization", employeeAuthToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new BusinessTravelRequestDto() {{
                    setCityId(1L); // Assuming Jakarta city has ID 1
                    setStartDate(LocalDate.now().plusDays(7));
                    setEndDate(LocalDate.now().plusDays(10));
                    setReason("Test travel by UUID");
                }})))
                .andExpect(status().isOk())
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        com.fasterxml.jackson.databind.JsonNode jsonNode = objectMapper.readTree(responseContent);
        String uuid = jsonNode.get("travelRequest").get("uuid").asText();

        // Then get by UUID
        mockMvc.perform(get("/api/business-travel/request/" + uuid)
                .header("Authorization", employeeAuthToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cityName").value("Jakarta"))
                .andExpect(jsonPath("$.uuid").value(uuid));
    }

    @Test
    void getBusinessTravelRequestByUuid_WithInvalidUuid_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/api/business-travel/request/" + UUID.randomUUID().toString())
                .header("Authorization", employeeAuthToken))
                .andExpect(status().isBadRequest());
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
        mockMvc.perform(get("/api/business-travel/supervisor/pending")
                .header("Authorization", supervisorAuthToken))
                .andExpect(status().isOk());
    }

    @Test
    void getPendingBusinessTravelRequests_WithEmployeeRole_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/api/business-travel/supervisor/pending")
                .header("Authorization", employeeAuthToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void createBusinessTravelRequest_OneDayTrip_ShouldCalculateOneDay() throws Exception {
        BusinessTravelRequestDto request = new BusinessTravelRequestDto();
        request.setCityId(2L); // Assuming Surabaya city has ID 2
        request.setStartDate(LocalDate.now().plusDays(7));
        request.setEndDate(LocalDate.now().plusDays(7)); // Same day
        request.setReason("One day business meeting");

        mockMvc.perform(post("/api/business-travel/request")
                .header("Authorization", employeeAuthToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.travelRequest.totalDays").value(1));
    }

    @Test
    void createBusinessTravelRequest_MultiDayTrip_ShouldCalculateCorrectDays() throws Exception {
        BusinessTravelRequestDto request = new BusinessTravelRequestDto();
        request.setCityId(3L); // Assuming Singapore city has ID 3
        request.setStartDate(LocalDate.now().plusDays(7));
        request.setEndDate(LocalDate.now().plusDays(10)); // 4 days total
        request.setReason("International conference");

        mockMvc.perform(post("/api/business-travel/request")
                .header("Authorization", employeeAuthToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.travelRequest.totalDays").value(4));
    }

    @Test
    void getCitiesDropdown_ShouldReturnListOfCities() throws Exception {
        mockMvc.perform(get("/api/business-travel/cities")
                .header("Authorization", employeeAuthToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[0].cityCode").exists())
                .andExpect(jsonPath("$[0].cityName").exists())
                .andExpect(jsonPath("$[0].provinceName").exists())
                .andExpect(jsonPath("$[0].displayText").exists());
    }

    @Test
    void getCitiesDropdownSearch_ShouldReturnFilteredCities() throws Exception {
        mockMvc.perform(get("/api/business-travel/cities")
                .param("search", "Jak")
                .header("Authorization", employeeAuthToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[?(@.cityName contains 'Jak')]").exists());
    }

    @Test
    void getCitiesDropdown_WithInvalidToken_ShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/business-travel/cities")
                .header("Authorization", "Bearer invalid.token"))
                .andExpect(status().isUnauthorized());
    }
}