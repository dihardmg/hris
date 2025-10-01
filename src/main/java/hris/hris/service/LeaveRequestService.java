package hris.hris.service;

import hris.hris.dto.LeaveRequestDto;
import hris.hris.model.Employee;
import hris.hris.model.LeaveRequest;
import hris.hris.repository.EmployeeRepository;
import hris.hris.repository.LeaveRequestRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class LeaveRequestService {

    @Autowired
    private LeaveRequestRepository leaveRequestRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Transactional
    public LeaveRequest createLeaveRequest(Long employeeId, LeaveRequestDto requestDto) {
        Employee employee = employeeRepository.findById(employeeId)
            .orElseThrow(() -> new RuntimeException("Employee not found"));

        if (requestDto.getStartDate().isAfter(requestDto.getEndDate())) {
            throw new RuntimeException("Start date cannot be after end date");
        }

        int overlappingLeaves = leaveRequestRepository.countOverlappingLeaves(
            employeeId,
            requestDto.getStartDate(),
            requestDto.getEndDate()
        );

        if (overlappingLeaves > 0) {
            throw new RuntimeException("You already have approved leave during this period");
        }

        validateLeaveBalance(employee, requestDto);

        LeaveRequest leaveRequest = new LeaveRequest();
        leaveRequest.setEmployee(employee);
        leaveRequest.setLeaveType(requestDto.getLeaveType());
        leaveRequest.setStartDate(requestDto.getStartDate());
        leaveRequest.setEndDate(requestDto.getEndDate());
        leaveRequest.setTotalDays(requestDto.getTotalDays());
        leaveRequest.setReason(requestDto.getReason());
        leaveRequest.setStatus(LeaveRequest.RequestStatus.PENDING);

        LeaveRequest savedRequest = leaveRequestRepository.save(leaveRequest);
        log.info("Leave request created for employee {} from {} to {}",
                employee.getEmployeeId(), requestDto.getStartDate(), requestDto.getEndDate());

        return savedRequest;
    }

    @Transactional
    public LeaveRequest approveLeaveRequest(Long requestId, Long supervisorId, String notes) {
        LeaveRequest leaveRequest = leaveRequestRepository.findById(requestId)
            .orElseThrow(() -> new RuntimeException("Leave request not found"));

        if (!leaveRequest.getStatus().equals(LeaveRequest.RequestStatus.PENDING)) {
            throw new RuntimeException("Leave request is not in pending status");
        }

        Employee supervisor = employeeRepository.findById(supervisorId)
            .orElseThrow(() -> new RuntimeException("Supervisor not found"));

        Employee employee = leaveRequest.getEmployee();
        if (!employee.getSupervisorId().equals(supervisorId)) {
            throw new RuntimeException("You are not authorized to approve this leave request");
        }

        validateLeaveBalance(employee, convertToDto(leaveRequest));

        deductLeaveBalance(employee, leaveRequest);

        leaveRequest.setStatus(LeaveRequest.RequestStatus.APPROVED);
        leaveRequest.setApprovedBy(supervisor);
        leaveRequest.setApprovalDate(java.time.LocalDateTime.now());
        leaveRequest.setRejectionReason(notes);

        LeaveRequest savedRequest = leaveRequestRepository.save(leaveRequest);
        log.info("Leave request {} approved by supervisor {}",
                requestId, supervisor.getEmployeeId());

        return savedRequest;
    }

    @Transactional
    public LeaveRequest rejectLeaveRequest(Long requestId, Long supervisorId, String rejectionReason) {
        LeaveRequest leaveRequest = leaveRequestRepository.findById(requestId)
            .orElseThrow(() -> new RuntimeException("Leave request not found"));

        if (!leaveRequest.getStatus().equals(LeaveRequest.RequestStatus.PENDING)) {
            throw new RuntimeException("Leave request is not in pending status");
        }

        Employee supervisor = employeeRepository.findById(supervisorId)
            .orElseThrow(() -> new RuntimeException("Supervisor not found"));

        Employee employee = leaveRequest.getEmployee();
        if (!employee.getSupervisorId().equals(supervisorId)) {
            throw new RuntimeException("You are not authorized to reject this leave request");
        }

        leaveRequest.setStatus(LeaveRequest.RequestStatus.REJECTED);
        leaveRequest.setApprovedBy(supervisor);
        leaveRequest.setApprovalDate(java.time.LocalDateTime.now());
        leaveRequest.setRejectionReason(rejectionReason);

        LeaveRequest savedRequest = leaveRequestRepository.save(leaveRequest);
        log.info("Leave request {} rejected by supervisor {} for reason: {}",
                requestId, supervisor.getEmployeeId(), rejectionReason);

        return savedRequest;
    }

    @Transactional(readOnly = true)
    public List<LeaveRequest> getEmployeeLeaveRequests(Long employeeId) {
        return leaveRequestRepository.findByEmployeeIdOrderByCreatedAtDesc(employeeId);
    }

    @Transactional(readOnly = true)
    public List<LeaveRequest> getPendingRequestsForSupervisor(Long supervisorId) {
        return leaveRequestRepository.findPendingRequestsBySupervisor(supervisorId);
    }

    @Transactional(readOnly = true)
    public Optional<LeaveRequest> getCurrentLeave(Long employeeId) {
        return leaveRequestRepository.findCurrentLeave(employeeId, LocalDate.now());
    }

    private void validateLeaveBalance(Employee employee, LeaveRequestDto requestDto) {
        int availableBalance = getAvailableLeaveBalance(employee, requestDto.getLeaveType());

        if (availableBalance < requestDto.getTotalDays()) {
            throw new RuntimeException(String.format(
                "Insufficient leave balance. Available: %d days, Requested: %d days",
                availableBalance, requestDto.getTotalDays()
            ));
        }
    }

    private int getAvailableLeaveBalance(Employee employee, LeaveRequest.LeaveType leaveType) {
        switch (leaveType) {
            case ANNUAL_LEAVE:
                return employee.getAnnualLeaveBalance();
            case SICK_LEAVE:
                return employee.getSickLeaveBalance();
            default:
                return employee.getAnnualLeaveBalance();
        }
    }

    private void deductLeaveBalance(Employee employee, LeaveRequest leaveRequest) {
        switch (leaveRequest.getLeaveType()) {
            case ANNUAL_LEAVE:
                employee.setAnnualLeaveBalance(
                    employee.getAnnualLeaveBalance() - leaveRequest.getTotalDays()
                );
                break;
            case SICK_LEAVE:
                employee.setSickLeaveBalance(
                    employee.getSickLeaveBalance() - leaveRequest.getTotalDays()
                );
                break;
        }
        employeeRepository.save(employee);
    }

    private LeaveRequestDto convertToDto(LeaveRequest leaveRequest) {
        LeaveRequestDto dto = new LeaveRequestDto();
        dto.setLeaveType(leaveRequest.getLeaveType());
        dto.setStartDate(leaveRequest.getStartDate());
        dto.setEndDate(leaveRequest.getEndDate());
        dto.setTotalDays(leaveRequest.getTotalDays());
        dto.setReason(leaveRequest.getReason());
        return dto;
    }
}