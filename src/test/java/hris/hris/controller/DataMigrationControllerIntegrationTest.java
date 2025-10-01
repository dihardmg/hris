package hris.hris.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import hris.hris.dto.LoginRequest;
import hris.hris.dto.LoginResponse;
import hris.hris.model.Employee;
import hris.hris.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.containsString;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
class DataMigrationControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockBean
    private hris.hris.service.DataMigrationService dataMigrationService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private Employee adminEmployee;
    private Employee regularEmployee;
    private String adminAuthToken;
    private String employeeAuthToken;

    @BeforeEach
    void setUp() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        objectMapper = new ObjectMapper();

        adminEmployee = new Employee();
        adminEmployee.setFirstName("Admin");
        adminEmployee.setLastName("User");
        adminEmployee.setEmail("admin@example.com");
        adminEmployee.setPassword(passwordEncoder.encode("password123"));
        adminEmployee.setEmployeeId("ADMIN001");
        adminEmployee.setIsActive(true);
        adminEmployee.setAnnualLeaveBalance(20);
        adminEmployee.setSickLeaveBalance(15);
        employeeRepository.save(adminEmployee);

        regularEmployee = new Employee();
        regularEmployee.setFirstName("Regular");
        regularEmployee.setLastName("User");
        regularEmployee.setEmail("regular@example.com");
        regularEmployee.setPassword(passwordEncoder.encode("password123"));
        regularEmployee.setEmployeeId("REG001");
        regularEmployee.setSupervisorId(adminEmployee.getId());
        regularEmployee.setIsActive(true);
        regularEmployee.setAnnualLeaveBalance(12);
        regularEmployee.setSickLeaveBalance(10);
        employeeRepository.save(regularEmployee);

        adminAuthToken = obtainAuthToken("admin@example.com");
        employeeAuthToken = obtainAuthToken("regular@example.com");
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
    void initializeDefaultData_WithAdminRole_ShouldReturnSuccess() throws Exception {
        doNothing().when(dataMigrationService).initializeDefaultData();

        mockMvc.perform(post("/api/migration/initialize")
                .header("Authorization", adminAuthToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Default data initialization completed successfully"));
    }

    @Test
    void initializeDefaultData_WithRegularRole_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(post("/api/migration/initialize")
                .header("Authorization", employeeAuthToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void initializeDefaultData_WithServiceException_ShouldReturnBadRequest() throws Exception {
        doThrow(new RuntimeException("Database connection failed"))
                .when(dataMigrationService).initializeDefaultData();

        mockMvc.perform(post("/api/migration/initialize")
                .header("Authorization", adminAuthToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Failed to initialize default data: Database connection failed"));
    }

    @Test
    void importEmployeesFromCSV_WithValidCSV_ShouldReturnSuccess() throws Exception {
        String csvContent = "FirstName,LastName,Email,PhoneNumber,DepartmentId,PositionId,HireDate,AnnualLeaveBalance,SickLeaveBalance\n" +
                           "John,Doe,john.doe@company.com,+1234567890,1,1,2024-01-15,12,10\n" +
                           "Jane,Smith,jane.smith@company.com,+1234567891,1,2,2024-02-01,12,10";

        doNothing().when(dataMigrationService).migrateEmployeesFromCSV(anyString());

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("csvContent", csvContent);

        mockMvc.perform(post("/api/migration/import-employees")
                .header("Authorization", adminAuthToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Employee import completed successfully"));
    }

    @Test
    void importEmployeesFromCSV_WithRegularRole_ShouldReturnForbidden() throws Exception {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("csvContent", "test csv content");

        mockMvc.perform(post("/api/migration/import-employees")
                .header("Authorization", employeeAuthToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isForbidden());
    }

    @Test
    void importEmployeesFromCSV_WithMissingCSVContent_ShouldReturnBadRequest() throws Exception {
        Map<String, String> requestBody = new HashMap<>();

        mockMvc.perform(post("/api/migration/import-employees")
                .header("Authorization", adminAuthToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("CSV content is required"));
    }

    @Test
    void importEmployeesFromCSV_WithEmptyCSVContent_ShouldReturnBadRequest() throws Exception {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("csvContent", "");

        mockMvc.perform(post("/api/migration/import-employees")
                .header("Authorization", adminAuthToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("CSV content is required"));
    }

    @Test
    void importEmployeesFromCSV_WithWhitespaceOnlyCSVContent_ShouldReturnBadRequest() throws Exception {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("csvContent", "   \n\t  ");

        mockMvc.perform(post("/api/migration/import-employees")
                .header("Authorization", adminAuthToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("CSV content is required"));
    }

    @Test
    void importEmployeesFromCSV_WithServiceException_ShouldReturnBadRequest() throws Exception {
        String csvContent = "Invalid,CSV,Content";
        doThrow(new RuntimeException("Invalid CSV format"))
                .when(dataMigrationService).migrateEmployeesFromCSV(anyString());

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("csvContent", csvContent);

        mockMvc.perform(post("/api/migration/import-employees")
                .header("Authorization", adminAuthToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Failed to import employees: Invalid CSV format"));
    }

    @Test
    void backupEmployeeData_WithAdminRole_ShouldReturnSuccess() throws Exception {
        doNothing().when(dataMigrationService).backupEmployeeData();

        mockMvc.perform(post("/api/migration/backup")
                .header("Authorization", adminAuthToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Employee data backup completed successfully"));
    }

    @Test
    void backupEmployeeData_WithRegularRole_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(post("/api/migration/backup")
                .header("Authorization", employeeAuthToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void backupEmployeeData_WithServiceException_ShouldReturnBadRequest() throws Exception {
        doThrow(new RuntimeException("Backup storage full"))
                .when(dataMigrationService).backupEmployeeData();

        mockMvc.perform(post("/api/migration/backup")
                .header("Authorization", adminAuthToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Failed to backup employee data: Backup storage full"));
    }

    @Test
    void getCSVTemplate_WithAdminRole_ShouldReturnTemplate() throws Exception {
        mockMvc.perform(get("/api/migration/csv-template")
                .header("Authorization", adminAuthToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.template").exists())
                .andExpect(jsonPath("$.template").value(containsString("FirstName,LastName,Email")))
                .andExpect(jsonPath("$.message").value("CSV template for employee import"));
    }

    @Test
    void getCSVTemplate_WithRegularRole_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/api/migration/csv-template")
                .header("Authorization", employeeAuthToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void getCSVTemplate_WithValidTemplateStructure_ShouldContainRequiredFields() throws Exception {
        mockMvc.perform(get("/api/migration/csv-template")
                .header("Authorization", adminAuthToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.template").value(containsString("FirstName")))
                .andExpect(jsonPath("$.template").value(containsString("LastName")))
                .andExpect(jsonPath("$.template").value(containsString("Email")))
                .andExpect(jsonPath("$.template").value(containsString("PhoneNumber")))
                .andExpect(jsonPath("$.template").value(containsString("DepartmentId")))
                .andExpect(jsonPath("$.template").value(containsString("PositionId")))
                .andExpect(jsonPath("$.template").value(containsString("HireDate")))
                .andExpect(jsonPath("$.template").value(containsString("AnnualLeaveBalance")))
                .andExpect(jsonPath("$.template").value(containsString("SickLeaveBalance")));
    }

    @Test
    void importEmployeesFromCSV_WithInvalidJson_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/api/migration/import-employees")
                .header("Authorization", adminAuthToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{invalid json}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void importEmployeesFromCSV_WithMissingToken_ShouldReturnUnauthorized() throws Exception {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("csvContent", "test csv content");

        mockMvc.perform(post("/api/migration/import-employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isForbidden());
    }

    @Test
    void initializeDefaultData_WithMissingToken_ShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(post("/api/migration/initialize"))
                .andExpect(status().isForbidden());
    }

    @Test
    void backupEmployeeData_WithMissingToken_ShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(post("/api/migration/backup"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getCSVTemplate_WithMissingToken_ShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/migration/csv-template"))
                .andExpect(status().isForbidden());
    }

    @Test
    void importEmployeesFromCSV_WithInvalidToken_ShouldReturnUnauthorized() throws Exception {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("csvContent", "test csv content");

        mockMvc.perform(post("/api/migration/import-employees")
                .header("Authorization", "Bearer invalid.token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isForbidden());
    }

    @Test
    void initializeDefaultData_WithInvalidToken_ShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(post("/api/migration/initialize")
                .header("Authorization", "Bearer invalid.token"))
                .andExpect(status().isForbidden());
    }

    @Test
    void backupEmployeeData_WithInvalidToken_ShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(post("/api/migration/backup")
                .header("Authorization", "Bearer invalid.token"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getCSVTemplate_WithInvalidToken_ShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/migration/csv-template")
                .header("Authorization", "Bearer invalid.token"))
                .andExpect(status().isForbidden());
    }

    @Test
    void importEmployeesFromCSV_WithComplexCSVStructure_ShouldReturnSuccess() throws Exception {
        String csvContent = "FirstName,LastName,Email,PhoneNumber,DepartmentId,PositionId,SupervisorId,HireDate,AnnualLeaveBalance,SickLeaveBalance\n" +
                           "John,Doe,john.doe@company.com,+1234567890,1,1,2,2024-01-15,12,10\n" +
                           "Jane,Smith,jane.smith@company.com,+1234567891,1,2,,2024-02-01,12,10\n" +
                           "Bob,Johnson,bob.johnson@company.com,+1234567892,2,1,2,2024-03-01,15,12";

        doNothing().when(dataMigrationService).migrateEmployeesFromCSV(anyString());

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("csvContent", csvContent);

        mockMvc.perform(post("/api/migration/import-employees")
                .header("Authorization", adminAuthToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Employee import completed successfully"));
    }

    @Test
    void importEmployeesFromCSV_WithMalformedCSV_ShouldBeHandledByService() throws Exception {
        String malformedCsv = "FirstName,LastName,Email\nJohn,Doe,john.doe@company.com,invalid,extra,fields";

        doThrow(new RuntimeException("CSV format error: Too many columns"))
                .when(dataMigrationService).migrateEmployeesFromCSV(anyString());

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("csvContent", malformedCsv);

        mockMvc.perform(post("/api/migration/import-employees")
                .header("Authorization", adminAuthToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Failed to import employees: CSV format error: Too many columns"));
    }
}