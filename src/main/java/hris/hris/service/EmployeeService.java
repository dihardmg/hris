package hris.hris.service;

import hris.hris.dto.EmployeeRegistrationDto;
import hris.hris.model.Employee;
import hris.hris.repository.EmployeeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class EmployeeService {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private FaceRecognitionService faceRecognitionService;

    @Transactional
    public Employee registerEmployee(EmployeeRegistrationDto registrationDto) {
        if (employeeRepository.existsByEmail(registrationDto.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        if (registrationDto.getSupervisorId() != null) {
            Optional<Employee> supervisor = employeeRepository.findById(registrationDto.getSupervisorId());
            if (supervisor.isEmpty()) {
                throw new RuntimeException("Supervisor not found");
            }
        }

        Employee employee = new Employee();
        employee.setFirstName(registrationDto.getFirstName());
        employee.setLastName(registrationDto.getLastName());
        employee.setEmail(registrationDto.getEmail());
        employee.setPassword(passwordEncoder.encode(registrationDto.getPassword()));
        employee.setPhoneNumber(registrationDto.getPhoneNumber());
        employee.setDepartmentId(registrationDto.getDepartmentId());
        employee.setPositionId(registrationDto.getPositionId());
        employee.setSupervisorId(registrationDto.getSupervisorId());
        employee.setHireDate(registrationDto.getHireDate());
        employee.setIsActive(true);

        if (registrationDto.getFaceImage() != null && !registrationDto.getFaceImage().isEmpty()) {
            if (faceRecognitionService.isFaceImageValid(registrationDto.getFaceImage())) {
                byte[] faceTemplate = faceRecognitionService.generateFaceTemplate(registrationDto.getFaceImage());
                // employee.setFaceTemplate(faceTemplate); // Temporarily disabled
                log.debug("Face template generated but not stored (feature disabled)");
            } else {
                throw new RuntimeException("Invalid face image format");
            }
        }

        Employee savedEmployee = employeeRepository.save(employee);
        log.info("New employee registered: {}", savedEmployee.getEmail());

        return savedEmployee;
    }

    @Transactional(readOnly = true)
    public List<Employee> getAllActiveEmployees() {
        return employeeRepository.findByIsActiveTrue();
    }

    @Transactional(readOnly = true)
    public List<Employee> getSubordinates(Long supervisorId) {
        return employeeRepository.findBySupervisorId(supervisorId);
    }

    @Transactional(readOnly = true)
    public Optional<Employee> getEmployeeById(Long id) {
        return employeeRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Employee> getEmployeeByEmployeeId(String employeeId) {
        return employeeRepository.findByEmployeeId(employeeId);
    }

    @Transactional
    public Employee updateEmployee(Long id, EmployeeRegistrationDto updateDto) {
        Employee employee = employeeRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Employee not found"));

        if (!employee.getEmail().equals(updateDto.getEmail()) &&
            employeeRepository.existsByEmail(updateDto.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        employee.setFirstName(updateDto.getFirstName());
        employee.setLastName(updateDto.getLastName());
        employee.setEmail(updateDto.getEmail());
        employee.setPhoneNumber(updateDto.getPhoneNumber());
        employee.setDepartmentId(updateDto.getDepartmentId());
        employee.setPositionId(updateDto.getPositionId());
        employee.setSupervisorId(updateDto.getSupervisorId());
        employee.setHireDate(updateDto.getHireDate());

        if (updateDto.getFaceImage() != null && !updateDto.getFaceImage().isEmpty()) {
            if (faceRecognitionService.isFaceImageValid(updateDto.getFaceImage())) {
                byte[] faceTemplate = faceRecognitionService.generateFaceTemplate(updateDto.getFaceImage());
                // employee.setFaceTemplate(faceTemplate); // Temporarily disabled
                log.debug("Face template generated but not stored (feature disabled)");
            } else {
                throw new RuntimeException("Invalid face image format");
            }
        }

        Employee updatedEmployee = employeeRepository.save(employee);
        log.info("Employee updated: {}", updatedEmployee.getEmail());

        return updatedEmployee;
    }

    @Transactional
    public void deactivateEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Employee not found"));

        employee.setIsActive(false);
        employeeRepository.save(employee);

        log.info("Employee deactivated: {}", employee.getEmail());
    }

    @Transactional
    public void activateEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Employee not found"));

        employee.setIsActive(true);
        employeeRepository.save(employee);

        log.info("Employee activated: {}", employee.getEmail());
    }

    @Transactional
    public Employee updateFaceTemplate(Long employeeId, String faceImage) {
        Employee employee = employeeRepository.findById(employeeId)
            .orElseThrow(() -> new RuntimeException("Employee not found"));

        if (faceRecognitionService.isFaceImageValid(faceImage)) {
            byte[] faceTemplate = faceRecognitionService.generateFaceTemplate(faceImage);
            // employee.setFaceTemplate(faceTemplate); // Temporarily disabled
            log.debug("Face template generated but not stored (feature disabled)");
            Employee updatedEmployee = employeeRepository.save(employee);
            log.info("Face validation completed for employee: {} (template storage disabled)", employee.getEmail());
            return updatedEmployee;
        } else {
            throw new RuntimeException("Invalid face image format");
        }
    }
}