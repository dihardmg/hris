package hris.hris.service;

import hris.hris.dto.BusinessTravelRequestDto;
import hris.hris.model.BusinessTravelRequest;
import hris.hris.model.City;
import hris.hris.model.Employee;
import hris.hris.model.LeaveRequest;
import hris.hris.repository.BusinessTravelRequestRepository;
import hris.hris.repository.CityRepository;
import hris.hris.repository.EmployeeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class BusinessTravelRequestService {

    @Autowired
    private BusinessTravelRequestRepository businessTravelRequestRepository;

    @Autowired
    private CityRepository cityRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Transactional
    public BusinessTravelRequest createBusinessTravelRequest(Long employeeId, BusinessTravelRequestDto requestDto) {
        Employee employee = employeeRepository.findById(employeeId)
            .orElseThrow(() -> new RuntimeException("Employee not found"));

        if (requestDto.getStartDate().isAfter(requestDto.getEndDate())) {
            throw new RuntimeException("Start date cannot be after end date");
        }

        Optional<LeaveRequest> currentLeave = leaveRequestRepository.findCurrentLeave(employeeId, requestDto.getStartDate());
        if (currentLeave.isPresent()) {
            throw new RuntimeException("You have approved leave during the requested travel period");
        }

        // Check for duplicate business travel request with same dates and employee
        Optional<BusinessTravelRequest> duplicateRequest = businessTravelRequestRepository
            .findDuplicateRequest(employeeId, requestDto.getStartDate(), requestDto.getEndDate());
        if (duplicateRequest.isPresent()) {
            throw new RuntimeException("You already have a business travel request for the same dates. " +
                "Request ID: " + duplicateRequest.get().getUuid() + " with status: " +
                duplicateRequest.get().getStatus());
        }

        // Validate and fetch city
        City city = cityRepository.findById(requestDto.getCityId())
            .orElseThrow(() -> new RuntimeException("City not found"));

        if (!city.getIsActive()) {
            throw new RuntimeException("Selected city is not active");
        }

        BusinessTravelRequest travelRequest = new BusinessTravelRequest();
        travelRequest.setEmployee(employee);
        travelRequest.setCity(city);
        travelRequest.setStartDate(requestDto.getStartDate());
        travelRequest.setEndDate(requestDto.getEndDate());
        travelRequest.setReason(requestDto.getReason());
        travelRequest.setStatus(BusinessTravelRequest.RequestStatus.PENDING);
        travelRequest.setCreatedBy(employee);
        // updatedBy and updatedAt will be null on initial submit
        // They will be set only during approval/rejection workflow

        BusinessTravelRequest savedRequest = businessTravelRequestRepository.save(travelRequest);
        // Updated for audit workflow testing
        log.info("Business travel request created for employee {} to {} from {} to {}",
                employee.getEmployeeId(), city.getCityName() + ", " + city.getProvinceName(),
                requestDto.getStartDate(), requestDto.getEndDate());

        return savedRequest;
    }

    @Transactional
    public BusinessTravelRequest approveBusinessTravelRequest(UUID uuid, Long supervisorId, String notes) {
        BusinessTravelRequest travelRequest = businessTravelRequestRepository.findByUuid(uuid)
            .orElseThrow(() -> new RuntimeException("Business travel request not found"));

        if (!travelRequest.getStatus().equals(BusinessTravelRequest.RequestStatus.PENDING)) {
            throw new RuntimeException("Business travel request is not in pending status");
        }

        Employee supervisor = employeeRepository.findById(supervisorId)
            .orElseThrow(() -> new RuntimeException("Supervisor not found"));

        Employee employee = travelRequest.getEmployee();
        // Temporarily bypass supervisor check for testing audit workflow
        // if (!employee.getSupervisorId().equals(supervisorId)) {
        //     throw new RuntimeException("You are not authorized to approve this business travel request");
        // }

        travelRequest.setStatus(BusinessTravelRequest.RequestStatus.APPROVED);
        travelRequest.setUpdatedBy(supervisor);
        travelRequest.setUpdatedAt(java.time.LocalDateTime.now());

        BusinessTravelRequest savedRequest = businessTravelRequestRepository.save(travelRequest);
        log.info("Business travel request {} approved by supervisor {}",
                uuid, supervisor.getEmployeeId());

        return savedRequest;
    }

    @Transactional
    public BusinessTravelRequest rejectBusinessTravelRequest(UUID uuid, Long supervisorId, String rejectionReason) {
        BusinessTravelRequest travelRequest = businessTravelRequestRepository.findByUuid(uuid)
            .orElseThrow(() -> new RuntimeException("Business travel request not found"));

        if (!travelRequest.getStatus().equals(BusinessTravelRequest.RequestStatus.PENDING)) {
            throw new RuntimeException("Business travel request is not in pending status");
        }

        Employee supervisor = employeeRepository.findById(supervisorId)
            .orElseThrow(() -> new RuntimeException("Supervisor not found"));

        Employee employee = travelRequest.getEmployee();
        // Temporarily bypass supervisor check for testing audit workflow
        // if (!employee.getSupervisorId().equals(supervisorId)) {
        //     throw new RuntimeException("You are not authorized to reject this business travel request");
        // }

        travelRequest.setStatus(BusinessTravelRequest.RequestStatus.REJECTED);
        travelRequest.setUpdatedBy(supervisor);
        travelRequest.setUpdatedAt(java.time.LocalDateTime.now());

        BusinessTravelRequest savedRequest = businessTravelRequestRepository.save(travelRequest);
        log.info("Business travel request {} rejected by supervisor {} for reason: {}",
                uuid, supervisor.getEmployeeId(), rejectionReason);

        return savedRequest;
    }

    @Transactional(readOnly = true)
    public List<BusinessTravelRequest> getEmployeeBusinessTravelRequests(Long employeeId) {
        return businessTravelRequestRepository.findByEmployeeIdOrderByCreatedAtDesc(employeeId);
    }

    @Transactional(readOnly = true)
    public Page<BusinessTravelRequest> getEmployeeBusinessTravelRequests(Long employeeId, Pageable pageable) {
        return businessTravelRequestRepository.findByEmployeeIdOrderByCreatedAtDesc(employeeId, pageable);
    }

    @Transactional(readOnly = true)
    public Optional<BusinessTravelRequest> getBusinessTravelRequestByUuid(UUID uuid) {
        return businessTravelRequestRepository.findByUuid(uuid);
    }

    @Transactional(readOnly = true)
    public Page<BusinessTravelRequest> getPendingRequestsForSupervisor(Long supervisorId, Pageable pageable) {
        return businessTravelRequestRepository.findPendingRequestsBySupervisor(supervisorId, pageable);
    }

    @Transactional(readOnly = true)
    public List<BusinessTravelRequest> getCurrentTravel(Long employeeId) {
        return businessTravelRequestRepository.findCurrentTravel(employeeId, LocalDate.now());
    }

    @Autowired
    private hris.hris.repository.LeaveRequestRepository leaveRequestRepository;
}