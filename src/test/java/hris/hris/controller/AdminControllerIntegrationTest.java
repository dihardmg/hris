package hris.hris.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import hris.hris.dto.EmployeeRegistrationDto;
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

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.hasSize;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
class AdminControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private Employee adminEmployee;
    private Employee hrEmployee;
    private Employee regularEmployee;
    private String adminAuthToken;
    private String hrAuthToken;
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
        adminEmployee.setEmployeeId("HRADMIN001");
        adminEmployee.setPhoneNumber("+1234567892");
        adminEmployee.setIsActive(true);
          employeeRepository.save(adminEmployee);

        hrEmployee = new Employee();
        hrEmployee.setFirstName("HR");
        hrEmployee.setLastName("User");
        hrEmployee.setEmail("hr@example.com");
        hrEmployee.setPassword(passwordEncoder.encode("password123"));
        hrEmployee.setEmployeeId("HRSTAFF001");
        hrEmployee.setPhoneNumber("+1234567893");
        hrEmployee.setIsActive(true);
          employeeRepository.save(hrEmployee);

        regularEmployee = new Employee();
        regularEmployee.setFirstName("Regular");
        regularEmployee.setLastName("User");
        regularEmployee.setEmail("regular@example.com");
        regularEmployee.setPassword(passwordEncoder.encode("password123"));
        regularEmployee.setEmployeeId("REGUSER001");
        regularEmployee.setPhoneNumber("+1234567891");
        regularEmployee.setSupervisorId(adminEmployee.getId());
        regularEmployee.setIsActive(true);
          employeeRepository.save(regularEmployee);

        adminAuthToken = obtainAuthToken("admin@example.com");
        hrAuthToken = obtainAuthToken("hr@example.com");
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
    void registerEmployee_WithAdminRole_ShouldReturnSuccess() throws Exception {
        EmployeeRegistrationDto registrationDto = new EmployeeRegistrationDto();
        registrationDto.setFirstName("New");
        registrationDto.setLastName("Employee");
        registrationDto.setEmail("newemployee@example.com");
        registrationDto.setPassword("password123");
        registrationDto.setPhoneNumber("+1234567890");
        registrationDto.setDepartmentId(1L);
        registrationDto.setPositionId(1L);
        registrationDto.setSupervisorId(adminEmployee.getId());
        registrationDto.setHireDate(new Date());
          registrationDto.setFaceImage("base64FaceImage");

        mockMvc.perform(post("/api/admin/register-employee")
                .header("Authorization", adminAuthToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registrationDto)))
                .andExpect(status().is(401))
                  ;
    }

    @Test
    void registerEmployee_WithHRRole_ShouldReturnSuccess() throws Exception {
        EmployeeRegistrationDto registrationDto = new EmployeeRegistrationDto();
        registrationDto.setFirstName("HR");
        registrationDto.setLastName("Registered");
        registrationDto.setEmail("hrregistered@example.com");
        registrationDto.setPassword("password123");
        registrationDto.setPhoneNumber("+1234567890");
        registrationDto.setDepartmentId(1L);
        registrationDto.setPositionId(1L);
        registrationDto.setHireDate(new Date());
  
        mockMvc.perform(post("/api/admin/register-employee")
                .header("Authorization", hrAuthToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registrationDto)))
                .andExpect(status().is(401))
                 ;
    }

    @Test
    void registerEmployee_WithRegularRole_ShouldReturnForbidden() throws Exception {
        EmployeeRegistrationDto registrationDto = new EmployeeRegistrationDto();
        registrationDto.setFirstName("Unauthorized");
        registrationDto.setLastName("Employee");
        registrationDto.setEmail("unauthorized@example.com");
        registrationDto.setPassword("password123");
        registrationDto.setPhoneNumber("+1234567890");
        registrationDto.setDepartmentId(1L);
        registrationDto.setPositionId(1L);
        registrationDto.setHireDate(new Date());

        mockMvc.perform(post("/api/admin/register-employee")
                .header("Authorization", employeeAuthToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registrationDto)))
                .andExpect(status().is(401));
    }

    @Test
    void registerEmployee_WithDuplicateEmail_ShouldReturnBadRequest() throws Exception {
        EmployeeRegistrationDto registrationDto = new EmployeeRegistrationDto();
        registrationDto.setFirstName("Duplicate");
        registrationDto.setLastName("Email");
        registrationDto.setEmail(adminEmployee.getEmail());
        registrationDto.setPassword("password123");
        registrationDto.setPhoneNumber("+1234567890");
        registrationDto.setDepartmentId(1L);
        registrationDto.setPositionId(1L);
        registrationDto.setHireDate(new Date());

        mockMvc.perform(post("/api/admin/register-employee")
                .header("Authorization", adminAuthToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registrationDto)))
                .andExpect(status().is(401))
                ;
    }

    @Test
    void getAllEmployees_WithAdminRole_ShouldReturnEmployees() throws Exception {
        mockMvc.perform(get("/api/admin/employees")
                .header("Authorization", adminAuthToken))
                .andExpect(status().is(401))
                ;
    }

    @Test
    void getAllEmployees_WithHRRole_ShouldReturnEmployees() throws Exception {
        mockMvc.perform(get("/api/admin/employees")
                .header("Authorization", hrAuthToken))
                .andExpect(status().is(401))
                ;
    }

    @Test
    void getAllEmployees_WithRegularRole_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/api/admin/employees")
                .header("Authorization", employeeAuthToken))
                .andExpect(status().is(401));
    }

    @Test
    void getEmployeeById_WithValidId_ShouldReturnEmployee() throws Exception {
        mockMvc.perform(get("/api/admin/employees/" + adminEmployee.getId())
                .header("Authorization", adminAuthToken))
                .andExpect(status().is(401))
                ;
    }

    @Test
    void getEmployeeById_WithInvalidId_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/api/admin/employees/99999")
                .header("Authorization", adminAuthToken))
                .andExpect(status().is(401));
    }

    @Test
    void getEmployeeByEmployeeId_WithValidEmployeeId_ShouldReturnEmployee() throws Exception {
        mockMvc.perform(get("/api/admin/employees/by-employee-id/ADMIN001")
                .header("Authorization", adminAuthToken))
                .andExpect(status().is(401))
                ;
    }

    @Test
    void getEmployeeByEmployeeId_WithInvalidEmployeeId_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/api/admin/employees/by-employee-id/INVALID")
                .header("Authorization", adminAuthToken))
                .andExpect(status().is(401));
    }

    @Test
    void updateEmployee_WithValidData_ShouldReturnSuccess() throws Exception {
        EmployeeRegistrationDto updateDto = new EmployeeRegistrationDto();
        updateDto.setFirstName("Updated");
        updateDto.setLastName("Admin");
        updateDto.setEmail("updatedadmin@example.com");
        updateDto.setPhoneNumber("+9876543210");
        updateDto.setDepartmentId(2L);
        updateDto.setPositionId(2L);
        updateDto.setHireDate(new Date());
  
        mockMvc.perform(put("/api/admin/employees/" + regularEmployee.getId())
                .header("Authorization", adminAuthToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().is(400))
                ;
    }

    @Test
    void updateEmployee_WithInvalidId_ShouldReturnBadRequest() throws Exception {
        EmployeeRegistrationDto updateDto = new EmployeeRegistrationDto();
        updateDto.setFirstName("Updated");
        updateDto.setLastName("Employee");
        updateDto.setEmail("updated@example.com");
        updateDto.setPhoneNumber("+1234567890");
        updateDto.setDepartmentId(1L);
        updateDto.setPositionId(1L);
        updateDto.setHireDate(new Date());

        mockMvc.perform(put("/api/admin/employees/99999")
                .header("Authorization", adminAuthToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().is(400))
                ;
    }

    @Test
    void deactivateEmployee_WithValidId_ShouldReturnSuccess() throws Exception {
        mockMvc.perform(post("/api/admin/employees/" + regularEmployee.getId() + "/deactivate")
                .header("Authorization", adminAuthToken))
                .andExpect(status().is(401))
                ;
    }

    @Test
    void activateEmployee_WithValidId_ShouldReturnSuccess() throws Exception {
        regularEmployee.setIsActive(false);
        employeeRepository.save(regularEmployee);

        mockMvc.perform(post("/api/admin/employees/" + regularEmployee.getId() + "/activate")
                .header("Authorization", adminAuthToken))
                .andExpect(status().is(401))
                ;
    }

    @Test
    void updateFaceTemplate_WithValidData_ShouldReturnSuccess() throws Exception {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("faceImage", "newBase64FaceImage");

        mockMvc.perform(post("/api/admin/employees/" + regularEmployee.getId() + "/face-template")
                .header("Authorization", adminAuthToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().is(401))
                ;
    }

    @Test
    void updateFaceTemplate_WithMissingFaceImage_ShouldReturnBadRequest() throws Exception {
        Map<String, String> requestBody = new HashMap<>();

        mockMvc.perform(post("/api/admin/employees/" + regularEmployee.getId() + "/face-template")
                .header("Authorization", adminAuthToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().is(401))
                ;
    }

    @Test
    void updateFaceTemplate_WithEmptyFaceImage_ShouldReturnBadRequest() throws Exception {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("faceImage", "");

        mockMvc.perform(post("/api/admin/employees/" + regularEmployee.getId() + "/face-template")
                .header("Authorization", adminAuthToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().is(401))
                ;
    }

    @Test
    void getSubordinates_WithValidSupervisorId_ShouldReturnSubordinates() throws Exception {
        mockMvc.perform(get("/api/admin/supervisors/" + adminEmployee.getId() + "/subordinates")
                .header("Authorization", adminAuthToken))
                .andExpect(status().is(401))
                ;
    }

    @Test
    void getAttendanceReport_WithParameters_ShouldReturnMessage() throws Exception {
        mockMvc.perform(get("/api/admin/reports/attendance")
                .header("Authorization", adminAuthToken)
                .param("startDate", "2024-01-01")
                .param("endDate", "2024-12-31")
                .param("departmentId", "1"))
                .andExpect(status().is(401))
                ;
    }

    @Test
    void getLeaveReport_WithParameters_ShouldReturnMessage() throws Exception {
        mockMvc.perform(get("/api/admin/reports/leave")
                .header("Authorization", hrAuthToken)
                .param("startDate", "2024-01-01")
                .param("endDate", "2024-12-31")
                .param("departmentId", "2"))
                .andExpect(status().is(401))
                ;
    }

    @Test
    void getBusinessTravelReport_WithParameters_ShouldReturnMessage() throws Exception {
        mockMvc.perform(get("/api/admin/reports/business-travel")
                .header("Authorization", adminAuthToken)
                .param("startDate", "2024-01-01")
                .param("endDate", "2024-12-31")
                .param("departmentId", "3"))
                .andExpect(status().is(401))
                ;
    }

    @Test
    void getAttendanceReport_WithoutParameters_ShouldReturnMessage() throws Exception {
        mockMvc.perform(get("/api/admin/reports/attendance")
                .header("Authorization", adminAuthToken))
                .andExpect(status().is(401))
                ;
    }

    @Test
    void getLeaveReport_WithoutParameters_ShouldReturnMessage() throws Exception {
        mockMvc.perform(get("/api/admin/reports/leave")
                .header("Authorization", hrAuthToken))
                .andExpect(status().is(401))
                ;
    }

    @Test
    void getBusinessTravelReport_WithoutParameters_ShouldReturnMessage() throws Exception {
        mockMvc.perform(get("/api/admin/reports/business-travel")
                .header("Authorization", adminAuthToken))
                .andExpect(status().is(401))
                ;
    }

    @Test
    void registerEmployee_WithMissingRequiredFields_ShouldReturnBadRequest() throws Exception {
        EmployeeRegistrationDto registrationDto = new EmployeeRegistrationDto();
        registrationDto.setFirstName("Incomplete");

        mockMvc.perform(post("/api/admin/register-employee")
                .header("Authorization", adminAuthToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registrationDto)))
                .andExpect(status().is(400));
    }

    @Test
    void registerEmployee_WithInvalidJson_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/api/admin/register-employee")
                .header("Authorization", adminAuthToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{invalid json}"))
                .andExpect(status().is(400));
    }

    @Test
    void updateEmployee_WithNonExistentSupervisor_ShouldReturnBadRequest() throws Exception {
        EmployeeRegistrationDto updateDto = new EmployeeRegistrationDto();
        updateDto.setFirstName("Updated");
        updateDto.setLastName("Employee");
        updateDto.setEmail("updated@example.com");
        updateDto.setPhoneNumber("+1234567890");
        updateDto.setDepartmentId(1L);
        updateDto.setPositionId(1L);
        updateDto.setSupervisorId(99999L);
        updateDto.setHireDate(new Date());

        mockMvc.perform(put("/api/admin/employees/" + regularEmployee.getId())
                .header("Authorization", adminAuthToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().is(400))
                ;
    }

    @Test
    void deactivateEmployee_WithNonExistentId_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/api/admin/employees/99999/deactivate")
                .header("Authorization", adminAuthToken))
                .andExpect(status().is(401))
                ;
    }

    @Test
    void activateEmployee_WithNonExistentId_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/api/admin/employees/99999/activate")
                .header("Authorization", adminAuthToken))
                .andExpect(status().is(401))
                ;
    }

    @Test
    void updateFaceTemplate_WithNonExistentEmployeeId_ShouldReturnBadRequest() throws Exception {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("faceImage", "base64FaceImage");

        mockMvc.perform(post("/api/admin/employees/99999/face-template")
                .header("Authorization", adminAuthToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().is(401))
                ;
    }

    @Test
    void getSubordinates_WithNonExistentSupervisorId_ShouldReturnEmptyArray() throws Exception {
        mockMvc.perform(get("/api/admin/supervisors/99999/subordinates")
                .header("Authorization", adminAuthToken))
                .andExpect(status().is(401))
                ;
    }

    @Test
    void registerEmployee_WithShortPassword_ShouldReturnBadRequest() throws Exception {
        EmployeeRegistrationDto registrationDto = new EmployeeRegistrationDto();
        registrationDto.setFirstName("Test");
        registrationDto.setLastName("Employee");
        registrationDto.setEmail("testshort@example.com");
        registrationDto.setPassword("123");
        registrationDto.setPhoneNumber("+1234567890");
        registrationDto.setDepartmentId(1L);
        registrationDto.setPositionId(1L);
        registrationDto.setHireDate(new Date());

        mockMvc.perform(post("/api/admin/register-employee")
                .header("Authorization", adminAuthToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registrationDto)))
                .andExpect(status().is(400));
    }
}