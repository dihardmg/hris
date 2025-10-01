package hris.hris.service;

import hris.hris.dto.BusinessTravelRequestDto;
import hris.hris.model.BusinessTravelRequest;
import hris.hris.model.Employee;
import hris.hris.model.LeaveRequest;
import hris.hris.repository.BusinessTravelRequestRepository;
import hris.hris.repository.EmployeeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class BusinessTravelRequestService {

    @Autowired
    private BusinessTravelRequestRepository businessTravelRequestRepository;

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

        BusinessTravelRequest travelRequest = new BusinessTravelRequest();
        travelRequest.setEmployee(employee);
        travelRequest.setTravelPurpose(requestDto.getTravelPurpose());
        travelRequest.setDestination(requestDto.getDestination());
        travelRequest.setStartDate(requestDto.getStartDate());
        travelRequest.setEndDate(requestDto.getEndDate());
        travelRequest.setEstimatedCost(requestDto.getEstimatedCost());
        travelRequest.setTransportationType(requestDto.getTransportationType());
        travelRequest.setAccommodationRequired(requestDto.getAccommodationRequired());
        travelRequest.setStatus(BusinessTravelRequest.RequestStatus.PENDING);

        BusinessTravelRequest savedRequest = businessTravelRequestRepository.save(travelRequest);
        log.info("Business travel request created for employee {} to {} from {} to {}",
                employee.getEmployeeId(), requestDto.getDestination(),
                requestDto.getStartDate(), requestDto.getEndDate());

        return savedRequest;
    }

    @Transactional
    public BusinessTravelRequest approveBusinessTravelRequest(Long requestId, Long supervisorId, String notes) {
        BusinessTravelRequest travelRequest = businessTravelRequestRepository.findById(requestId)
            .orElseThrow(() -> new RuntimeException("Business travel request not found"));

        if (!travelRequest.getStatus().equals(BusinessTravelRequest.RequestStatus.PENDING)) {
            throw new RuntimeException("Business travel request is not in pending status");
        }

        Employee supervisor = employeeRepository.findById(supervisorId)
            .orElseThrow(() -> new RuntimeException("Supervisor not found"));

        Employee employee = travelRequest.getEmployee();
        if (!employee.getSupervisorId().equals(supervisorId)) {
            throw new RuntimeException("You are not authorized to approve this business travel request");
        }

        travelRequest.setStatus(BusinessTravelRequest.RequestStatus.APPROVED);
        travelRequest.setApprovedBy(supervisor);
        travelRequest.setApprovalDate(java.time.LocalDateTime.now());
        travelRequest.setRejectionReason(notes);

        BusinessTravelRequest savedRequest = businessTravelRequestRepository.save(travelRequest);
        log.info("Business travel request {} approved by supervisor {}",
                requestId, supervisor.getEmployeeId());

        return savedRequest;
    }

    @Transactional
    public BusinessTravelRequest rejectBusinessTravelRequest(Long requestId, Long supervisorId, String rejectionReason) {
        BusinessTravelRequest travelRequest = businessTravelRequestRepository.findById(requestId)
            .orElseThrow(() -> new RuntimeException("Business travel request not found"));

        if (!travelRequest.getStatus().equals(BusinessTravelRequest.RequestStatus.PENDING)) {
            throw new RuntimeException("Business travel request is not in pending status");
        }

        Employee supervisor = employeeRepository.findById(supervisorId)
            .orElseThrow(() -> new RuntimeException("Supervisor not found"));

        Employee employee = travelRequest.getEmployee();
        if (!employee.getSupervisorId().equals(supervisorId)) {
            throw new RuntimeException("You are not authorized to reject this business travel request");
        }

        travelRequest.setStatus(BusinessTravelRequest.RequestStatus.REJECTED);
        travelRequest.setApprovedBy(supervisor);
        travelRequest.setApprovalDate(java.time.LocalDateTime.now());
        travelRequest.setRejectionReason(rejectionReason);

        BusinessTravelRequest savedRequest = businessTravelRequestRepository.save(travelRequest);
        log.info("Business travel request {} rejected by supervisor {} for reason: {}",
                requestId, supervisor.getEmployeeId(), rejectionReason);

        return savedRequest;
    }

    @Transactional(readOnly = true)
    public List<BusinessTravelRequest> getEmployeeBusinessTravelRequests(Long employeeId) {
        return businessTravelRequestRepository.findByEmployeeIdOrderByCreatedAtDesc(employeeId);
    }

    @Transactional(readOnly = true)
    public List<BusinessTravelRequest> getPendingRequestsForSupervisor(Long supervisorId) {
        return businessTravelRequestRepository.findPendingRequestsBySupervisor(supervisorId);
    }

    @Transactional(readOnly = true)
    public List<BusinessTravelRequest> getCurrentTravel(Long employeeId) {
        return businessTravelRequestRepository.findCurrentTravel(employeeId, LocalDate.now());
    }

    @Autowired
    private hris.hris.repository.LeaveRequestRepository leaveRequestRepository;
}