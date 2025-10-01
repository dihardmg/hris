package hris.hris.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import hris.hris.dto.LoginRequest;
import hris.hris.dto.LoginResponse;
import hris.hris.model.Employee;
import hris.hris.repository.EmployeeRepository;
import org.flywaydb.core.api.output.MigrateResult;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
class FlywayControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockBean
    private hris.hris.service.FlywayService flywayService;

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
    void migrate_WithAdminRole_ShouldReturnSuccess() throws Exception {
        MigrateResult mockResult = mock(MigrateResult.class);
        when(mockResult.migrationsExecuted).thenReturn(2);
        when(mockResult.targetSchemaVersion).thenReturn("2.0.0");
        when(mockResult.success).thenReturn(true);

        when(flywayService.migrate()).thenReturn(mockResult);

        mockMvc.perform(post("/api/flyway/migrate")
                .header("Authorization", adminAuthToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Migration completed successfully"))
                .andExpect(jsonPath("$.migrationsExecuted").value(2))
                .andExpect(jsonPath("$.flywayVersion").value("2.0.0"));
    }

    @Test
    void migrate_WithRegularRole_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(post("/api/flyway/migrate")
                .header("Authorization", employeeAuthToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void migrate_WithServiceException_ShouldReturnInternalServerError() throws Exception {
        when(flywayService.migrate()).thenThrow(new RuntimeException("Migration failed due to database error"));

        mockMvc.perform(post("/api/flyway/migrate")
                .header("Authorization", adminAuthToken))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Migration failed: Migration failed due to database error"));
    }

    @Test
    void getStatus_WithAdminRole_ShouldReturnStatus() throws Exception {
        Map<String, Object> mockStatus = new HashMap<>();
        mockStatus.put("schemaVersion", "2");
        mockStatus.put("pendingCount", 0);
        mockStatus.put("appliedCount", 12);
        mockStatus.put("database", "PostgreSQL");
        mockStatus.put("valid", true);

        when(flywayService.getMigrationStatus()).thenReturn(mockStatus);

        mockMvc.perform(get("/api/flyway/status")
                .header("Authorization", adminAuthToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.schemaVersion").value(2))
                .andExpect(jsonPath("$.pendingCount").value(0))
                .andExpect(jsonPath("$.appliedCount").value(12))
                .andExpect(jsonPath("$.database").value("PostgreSQL"))
                .andExpect(jsonPath("$.valid").value(true));
    }

    @Test
    void getStatus_WithRegularRole_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/api/flyway/status")
                .header("Authorization", employeeAuthToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void getStatus_WithServiceException_ShouldReturnInternalServerError() throws Exception {
        when(flywayService.getMigrationStatus()).thenThrow(new RuntimeException("Failed to get status"));

        mockMvc.perform(get("/api/flyway/status")
                .header("Authorization", adminAuthToken))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Failed to get migration status: Failed to get status"));
    }

    @Test
    void getPendingMigrations_WithAdminRole_ShouldReturnPending() throws Exception {
        String[] pendingMigrations = {"V13__Add_new_feature.sql", "V14__Update_schema.sql"};

        when(flywayService.getPendingMigrations()).thenReturn(pendingMigrations);

        mockMvc.perform(get("/api/flyway/pending")
                .header("Authorization", adminAuthToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pendingMigrations").isArray())
                .andExpect(jsonPath("$.pendingMigrations[0]").value("V13__Add_new_feature.sql"))
                .andExpect(jsonPath("$.pendingMigrations[1]").value("V14__Update_schema.sql"))
                .andExpect(jsonPath("$.count").value(2));
    }

    @Test
    void getPendingMigrations_WithNoPendingMigrations_ShouldReturnEmpty() throws Exception {
        String[] pendingMigrations = {};

        when(flywayService.getPendingMigrations()).thenReturn(pendingMigrations);

        mockMvc.perform(get("/api/flyway/pending")
                .header("Authorization", adminAuthToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pendingMigrations").isArray())
                .andExpect(jsonPath("$.pendingMigrations").isEmpty())
                .andExpect(jsonPath("$.count").value(0));
    }

    @Test
    void getPendingMigrations_WithRegularRole_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/api/flyway/pending")
                .header("Authorization", employeeAuthToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void getPendingMigrations_WithServiceException_ShouldReturnInternalServerError() throws Exception {
        when(flywayService.getPendingMigrations()).thenThrow(new RuntimeException("Failed to get pending migrations"));

        mockMvc.perform(get("/api/flyway/pending")
                .header("Authorization", adminAuthToken))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Failed to get pending migrations: Failed to get pending migrations"));
    }

    @Test
    void getAppliedMigrations_WithAdminRole_ShouldReturnApplied() throws Exception {
        String[] appliedMigrations = {"V1__Create_employees_table.sql", "V2__Create_attendances_table.sql", "V3__Create_leave_requests_table.sql"};

        when(flywayService.getAppliedMigrations()).thenReturn(appliedMigrations);

        mockMvc.perform(get("/api/flyway/applied")
                .header("Authorization", adminAuthToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.appliedMigrations").isArray())
                .andExpect(jsonPath("$.appliedMigrations[0]").value("V1__Create_employees_table.sql"))
                .andExpect(jsonPath("$.appliedMigrations[1]").value("V2__Create_attendances_table.sql"))
                .andExpect(jsonPath("$.appliedMigrations[2]").value("V3__Create_leave_requests_table.sql"))
                .andExpect(jsonPath("$.count").value(3));
    }

    @Test
    void getAppliedMigrations_WithNoAppliedMigrations_ShouldReturnEmpty() throws Exception {
        String[] appliedMigrations = {};

        when(flywayService.getAppliedMigrations()).thenReturn(appliedMigrations);

        mockMvc.perform(get("/api/flyway/applied")
                .header("Authorization", adminAuthToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.appliedMigrations").isArray())
                .andExpect(jsonPath("$.appliedMigrations").isEmpty())
                .andExpect(jsonPath("$.count").value(0));
    }

    @Test
    void getAppliedMigrations_WithRegularRole_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/api/flyway/applied")
                .header("Authorization", employeeAuthToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAppliedMigrations_WithServiceException_ShouldReturnInternalServerError() throws Exception {
        when(flywayService.getAppliedMigrations()).thenThrow(new RuntimeException("Failed to get applied migrations"));

        mockMvc.perform(get("/api/flyway/applied")
                .header("Authorization", adminAuthToken))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Failed to get applied migrations: Failed to get applied migrations"));
    }

    @Test
    void validate_WithValidSchema_ShouldReturnTrue() throws Exception {
        when(flywayService.validate()).thenReturn(true);

        mockMvc.perform(post("/api/flyway/validate")
                .header("Authorization", adminAuthToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true))
                .andExpect(jsonPath("$.message").value("Validation passed"));
    }

    @Test
    void validate_WithInvalidSchema_ShouldReturnFalse() throws Exception {
        when(flywayService.validate()).thenReturn(false);

        mockMvc.perform(post("/api/flyway/validate")
                .header("Authorization", adminAuthToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(false))
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    void validate_WithRegularRole_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(post("/api/flyway/validate")
                .header("Authorization", employeeAuthToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void validate_WithServiceException_ShouldReturnInternalServerError() throws Exception {
        when(flywayService.validate()).thenThrow(new RuntimeException("Validation service error"));

        mockMvc.perform(post("/api/flyway/validate")
                .header("Authorization", adminAuthToken))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Validation failed: Validation service error"));
    }

    @Test
    void repair_WithAdminRole_ShouldReturnSuccess() throws Exception {
        doNothing().when(flywayService).repair();

        mockMvc.perform(post("/api/flyway/repair")
                .header("Authorization", adminAuthToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Repair completed successfully"));
    }

    @Test
    void repair_WithRegularRole_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(post("/api/flyway/repair")
                .header("Authorization", employeeAuthToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void repair_WithServiceException_ShouldReturnInternalServerError() throws Exception {
        doThrow(new RuntimeException("Repair failed")).when(flywayService).repair();

        mockMvc.perform(post("/api/flyway/repair")
                .header("Authorization", adminAuthToken))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Repair failed: Repair failed"));
    }

    @Test
    void baseline_WithAdminRole_ShouldReturnSuccess() throws Exception {
        doNothing().when(flywayService).baseline();

        mockMvc.perform(post("/api/flyway/baseline")
                .header("Authorization", adminAuthToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Baseline completed successfully"));
    }

    @Test
    void baseline_WithRegularRole_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(post("/api/flyway/baseline")
                .header("Authorization", employeeAuthToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void baseline_WithServiceException_ShouldReturnInternalServerError() throws Exception {
        doThrow(new RuntimeException("Baseline failed")).when(flywayService).baseline();

        mockMvc.perform(post("/api/flyway/baseline")
                .header("Authorization", adminAuthToken))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Baseline failed: Baseline failed"));
    }

    @Test
    void clean_WithAdminRole_ShouldReturnSuccess() throws Exception {
        doNothing().when(flywayService).clean();

        mockMvc.perform(post("/api/flyway/clean")
                .header("Authorization", adminAuthToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Clean completed successfully"));
    }

    @Test
    void clean_WithRegularRole_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(post("/api/flyway/clean")
                .header("Authorization", employeeAuthToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void clean_WithServiceException_ShouldReturnInternalServerError() throws Exception {
        doThrow(new RuntimeException("Clean failed")).when(flywayService).clean();

        mockMvc.perform(post("/api/flyway/clean")
                .header("Authorization", adminAuthToken))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Clean failed: Clean failed"));
    }

    @Test
    void migrate_WithMissingToken_ShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(post("/api/flyway/migrate"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getStatus_WithMissingToken_ShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/flyway/status"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getPendingMigrations_WithMissingToken_ShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/flyway/pending"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAppliedMigrations_WithMissingToken_ShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/flyway/applied"))
                .andExpect(status().isForbidden());
    }

    @Test
    void validate_WithMissingToken_ShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(post("/api/flyway/validate"))
                .andExpect(status().isForbidden());
    }

    @Test
    void repair_WithMissingToken_ShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(post("/api/flyway/repair"))
                .andExpect(status().isForbidden());
    }

    @Test
    void baseline_WithMissingToken_ShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(post("/api/flyway/baseline"))
                .andExpect(status().isForbidden());
    }

    @Test
    void clean_WithMissingToken_ShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(post("/api/flyway/clean"))
                .andExpect(status().isForbidden());
    }

    @Test
    void migrate_WithInvalidToken_ShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(post("/api/flyway/migrate")
                .header("Authorization", "Bearer invalid.token"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getStatus_WithInvalidToken_ShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/flyway/status")
                .header("Authorization", "Bearer invalid.token"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getPendingMigrations_WithInvalidToken_ShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/flyway/pending")
                .header("Authorization", "Bearer invalid.token"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAppliedMigrations_WithInvalidToken_ShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/flyway/applied")
                .header("Authorization", "Bearer invalid.token"))
                .andExpect(status().isForbidden());
    }

    @Test
    void validate_WithInvalidToken_ShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(post("/api/flyway/validate")
                .header("Authorization", "Bearer invalid.token"))
                .andExpect(status().isForbidden());
    }

    @Test
    void repair_WithInvalidToken_ShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(post("/api/flyway/repair")
                .header("Authorization", "Bearer invalid.token"))
                .andExpect(status().isForbidden());
    }

    @Test
    void baseline_WithInvalidToken_ShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(post("/api/flyway/baseline")
                .header("Authorization", "Bearer invalid.token"))
                .andExpect(status().isForbidden());
    }

    @Test
    void clean_WithInvalidToken_ShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(post("/api/flyway/clean")
                .header("Authorization", "Bearer invalid.token"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getMigrationStatus_WithComplexStatus_ShouldReturnCompleteStatus() throws Exception {
        Map<String, Object> complexStatus = new HashMap<>();
        complexStatus.put("schemaVersion", "12");
        complexStatus.put("pendingCount", 3);
        complexStatus.put("appliedCount", 12);
        complexStatus.put("database", "PostgreSQL 18.0");
        complexStatus.put("valid", true);
        complexStatus.put("lastMigration", "V12__Fix_face_template_column.sql");
        complexStatus.put("baselineVersion", "1");
        complexStatus.put("installedBy", "flyway");

        when(flywayService.getMigrationStatus()).thenReturn(complexStatus);

        mockMvc.perform(get("/api/flyway/status")
                .header("Authorization", adminAuthToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.schemaVersion").value(12))
                .andExpect(jsonPath("$.pendingCount").value(3))
                .andExpect(jsonPath("$.appliedCount").value(12))
                .andExpect(jsonPath("$.database").value("PostgreSQL 18.0"))
                .andExpect(jsonPath("$.valid").value(true))
                .andExpect(jsonPath("$.lastMigration").value("V12__Fix_face_template_column.sql"))
                .andExpect(jsonPath("$.baselineVersion").value(1))
                .andExpect(jsonPath("$.installedBy").value("flyway"));
    }
}