package hris.hris.service;

import hris.hris.dto.EmployeeRegistrationDto;
import hris.hris.model.Employee;
import hris.hris.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private FaceRecognitionService faceRecognitionService;

    @InjectMocks
    private EmployeeService employeeService;

    private EmployeeRegistrationDto registrationDto;
    private Employee employee;
    private Employee supervisor;

    @BeforeEach
    void setUp() {
        registrationDto = new EmployeeRegistrationDto();
        registrationDto.setFirstName("John");
        registrationDto.setLastName("Doe");
        registrationDto.setEmail("john.doe@example.com");
        registrationDto.setPassword("password123");
        registrationDto.setPhoneNumber("1234567890");
        registrationDto.setDepartmentId(1L);
        registrationDto.setPositionId(1L);
        registrationDto.setSupervisorId(2L);
        registrationDto.setHireDate(new Date(2023 - 1900, 0, 1));
        registrationDto.setAnnualLeaveBalance(12);
        registrationDto.setSickLeaveBalance(5);
        registrationDto.setFaceImage("base64FaceImage");

        employee = new Employee();
        employee.setId(1L);
        employee.setFirstName("John");
        employee.setLastName("Doe");
        employee.setEmail("john.doe@example.com");
        employee.setIsActive(true);

        supervisor = new Employee();
        supervisor.setId(2L);
        supervisor.setFirstName("Jane");
        supervisor.setLastName("Smith");
        supervisor.setEmail("jane.smith@example.com");
    }

    @Test
    void registerEmployee_WithValidData_ShouldReturnSavedEmployee() {
        when(employeeRepository.existsByEmail("john.doe@example.com")).thenReturn(false);
        when(employeeRepository.findById(2L)).thenReturn(Optional.of(supervisor));
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(faceRecognitionService.isFaceImageValid("base64FaceImage")).thenReturn(true);
        when(faceRecognitionService.generateFaceTemplate("base64FaceImage")).thenReturn(new byte[]{1, 2, 3});
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);

        Employee result = employeeService.registerEmployee(registrationDto);

        assertNotNull(result);
        assertEquals("john.doe@example.com", result.getEmail());

        verify(employeeRepository).existsByEmail("john.doe@example.com");
        verify(employeeRepository).findById(2L);
        verify(passwordEncoder).encode("password123");
        verify(faceRecognitionService).isFaceImageValid("base64FaceImage");
        verify(faceRecognitionService).generateFaceTemplate("base64FaceImage");
        verify(employeeRepository).save(any(Employee.class));
    }

    @Test
    void registerEmployee_WithExistingEmail_ShouldThrowRuntimeException() {
        when(employeeRepository.existsByEmail("john.doe@example.com")).thenReturn(true);

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> employeeService.registerEmployee(registrationDto)
        );

        assertEquals("Email already exists", exception.getMessage());

        verify(employeeRepository).existsByEmail("john.doe@example.com");
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    void registerEmployee_WithNonExistentSupervisor_ShouldThrowRuntimeException() {
        when(employeeRepository.existsByEmail("john.doe@example.com")).thenReturn(false);
        when(employeeRepository.findById(2L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> employeeService.registerEmployee(registrationDto)
        );

        assertEquals("Supervisor not found", exception.getMessage());

        verify(employeeRepository).existsByEmail("john.doe@example.com");
        verify(employeeRepository).findById(2L);
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    void registerEmployee_WithInvalidFaceImage_ShouldThrowRuntimeException() {
        when(employeeRepository.existsByEmail("john.doe@example.com")).thenReturn(false);
        when(employeeRepository.findById(2L)).thenReturn(Optional.of(supervisor));
        when(faceRecognitionService.isFaceImageValid("base64FaceImage")).thenReturn(false);

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> employeeService.registerEmployee(registrationDto)
        );

        assertEquals("Invalid face image format", exception.getMessage());

        verify(employeeRepository).existsByEmail("john.doe@example.com");
        verify(employeeRepository).findById(2L);
        verify(faceRecognitionService).isFaceImageValid("base64FaceImage");
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    void getAllActiveEmployees_ShouldReturnActiveEmployees() {
        List<Employee> employees = Arrays.asList(employee, supervisor);
        when(employeeRepository.findByIsActiveTrue()).thenReturn(employees);

        List<Employee> result = employeeService.getAllActiveEmployees();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("john.doe@example.com", result.get(0).getEmail());

        verify(employeeRepository).findByIsActiveTrue();
    }

    @Test
    void getSubordinates_ShouldReturnSubordinates() {
        List<Employee> subordinates = Arrays.asList(employee);
        when(employeeRepository.findBySupervisorId(2L)).thenReturn(subordinates);

        List<Employee> result = employeeService.getSubordinates(2L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("john.doe@example.com", result.get(0).getEmail());

        verify(employeeRepository).findBySupervisorId(2L);
    }

    @Test
    void getEmployeeById_WithExistingId_ShouldReturnEmployee() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

        Optional<Employee> result = employeeService.getEmployeeById(1L);

        assertTrue(result.isPresent());
        assertEquals("john.doe@example.com", result.get().getEmail());

        verify(employeeRepository).findById(1L);
    }

    @Test
    void getEmployeeById_WithNonExistingId_ShouldReturnEmpty() {
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<Employee> result = employeeService.getEmployeeById(999L);

        assertFalse(result.isPresent());

        verify(employeeRepository).findById(999L);
    }

    @Test
    void getEmployeeByEmployeeId_WithExistingEmployeeId_ShouldReturnEmployee() {
        when(employeeRepository.findByEmployeeId("EMP001")).thenReturn(Optional.of(employee));

        Optional<Employee> result = employeeService.getEmployeeByEmployeeId("EMP001");

        assertTrue(result.isPresent());
        assertEquals("john.doe@example.com", result.get().getEmail());

        verify(employeeRepository).findByEmployeeId("EMP001");
    }

    @Test
    void getEmployeeByEmployeeId_WithNonExistingEmployeeId_ShouldReturnEmpty() {
        when(employeeRepository.findByEmployeeId("NONEXISTENT")).thenReturn(Optional.empty());

        Optional<Employee> result = employeeService.getEmployeeByEmployeeId("NONEXISTENT");

        assertFalse(result.isPresent());

        verify(employeeRepository).findByEmployeeId("NONEXISTENT");
    }

    @Test
    void updateEmployee_WithValidData_ShouldReturnUpdatedEmployee() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeRepository.existsByEmail("new.email@example.com")).thenReturn(false);
        when(faceRecognitionService.isFaceImageValid("newFaceImage")).thenReturn(true);
        when(faceRecognitionService.generateFaceTemplate("newFaceImage")).thenReturn(new byte[]{4, 5, 6});
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);

        registrationDto.setEmail("new.email@example.com");
        registrationDto.setFaceImage("newFaceImage");

        Employee result = employeeService.updateEmployee(1L, registrationDto);

        assertNotNull(result);

        verify(employeeRepository).findById(1L);
        verify(employeeRepository).existsByEmail("new.email@example.com");
        verify(faceRecognitionService).isFaceImageValid("newFaceImage");
        verify(faceRecognitionService).generateFaceTemplate("newFaceImage");
        verify(employeeRepository).save(any(Employee.class));
    }

    @Test
    void updateEmployee_WithNonExistingId_ShouldThrowRuntimeException() {
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> employeeService.updateEmployee(999L, registrationDto)
        );

        assertEquals("Employee not found", exception.getMessage());

        verify(employeeRepository).findById(999L);
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    void updateEmployee_WithDuplicateEmail_ShouldThrowRuntimeException() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeRepository.existsByEmail("different.email@example.com")).thenReturn(true);

        registrationDto.setEmail("different.email@example.com");

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> employeeService.updateEmployee(1L, registrationDto)
        );

        assertEquals("Email already exists", exception.getMessage());

        verify(employeeRepository).findById(1L);
        verify(employeeRepository).existsByEmail("different.email@example.com");
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    void deactivateEmployee_WithExistingId_ShouldDeactivateEmployee() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);

        employeeService.deactivateEmployee(1L);

        verify(employeeRepository).findById(1L);
        verify(employeeRepository).save(employee);
        assertFalse(employee.getIsActive());
    }

    @Test
    void deactivateEmployee_WithNonExistingId_ShouldThrowRuntimeException() {
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> employeeService.deactivateEmployee(999L)
        );

        assertEquals("Employee not found", exception.getMessage());

        verify(employeeRepository).findById(999L);
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    void activateEmployee_WithExistingId_ShouldActivateEmployee() {
        employee.setIsActive(false);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);

        employeeService.activateEmployee(1L);

        verify(employeeRepository).findById(1L);
        verify(employeeRepository).save(employee);
        assertTrue(employee.getIsActive());
    }

    @Test
    void updateFaceTemplate_WithValidFaceImage_ShouldReturnUpdatedEmployee() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(faceRecognitionService.isFaceImageValid("newFaceImage")).thenReturn(true);
        when(faceRecognitionService.generateFaceTemplate("newFaceImage")).thenReturn(new byte[]{7, 8, 9});
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);

        Employee result = employeeService.updateFaceTemplate(1L, "newFaceImage");

        assertNotNull(result);

        verify(employeeRepository).findById(1L);
        verify(faceRecognitionService).isFaceImageValid("newFaceImage");
        verify(faceRecognitionService).generateFaceTemplate("newFaceImage");
        verify(employeeRepository).save(any(Employee.class));
    }

    @Test
    void updateFaceTemplate_WithInvalidFaceImage_ShouldThrowRuntimeException() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(faceRecognitionService.isFaceImageValid("invalidFaceImage")).thenReturn(false);

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> employeeService.updateFaceTemplate(1L, "invalidFaceImage")
        );

        assertEquals("Invalid face image format", exception.getMessage());

        verify(employeeRepository).findById(1L);
        verify(faceRecognitionService).isFaceImageValid("invalidFaceImage");
        verify(employeeRepository, never()).save(any(Employee.class));
    }
}