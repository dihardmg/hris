package hris.hris.service;

import hris.hris.dto.LeaveRequestDto;
import hris.hris.model.Employee;
import hris.hris.model.LeaveRequest;
import hris.hris.model.LeaveType;
import hris.hris.repository.EmployeeRepository;
import hris.hris.repository.LeaveRequestRepository;
import hris.hris.repository.LeaveTypeRepository;
import hris.hris.exception.LeaveRequestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class LeaveRequestService {

    @Autowired
    private LeaveRequestRepository leaveRequestRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private LeaveTypeRepository leaveTypeRepository;

    @Transactional
    public LeaveRequest createLeaveRequest(Long employeeId, LeaveRequestDto requestDto) {
        Employee employee = employeeRepository.findById(employeeId)
            .orElseThrow(() -> new RuntimeException("Employee not found"));

        if (requestDto.getStartDate().isAfter(requestDto.getEndDate())) {
            throw new RuntimeException("Start date cannot be after end date");
        }

        // Validate that dates are not in the past
        LocalDate today = LocalDate.now();
        if (requestDto.getStartDate().isBefore(today)) {
            throw new RuntimeException("Start date cannot be in the past");
        }
        if (requestDto.getEndDate().isBefore(today)) {
            throw new RuntimeException("End date cannot be in the past");
        }

        // Get LeaveType entity
        LeaveType leaveType = leaveTypeRepository.findById(requestDto.getLeaveTypeId())
            .orElseThrow(() -> new RuntimeException("Leave type not found"));

        if (!leaveType.getIsActive()) {
            throw new RuntimeException("Leave type is not active");
        }

        // Validate minimum duration if specified
        if (leaveType.getMinDurationDays() != null) {
            long requestedDays = java.time.temporal.ChronoUnit.DAYS.between(
                requestDto.getStartDate(),
                requestDto.getEndDate()
            ) + 1;
            if (requestedDays < leaveType.getMinDurationDays()) {
                throw new RuntimeException(String.format(
                    "Minimum duration for %s is %d days",
                    leaveType.getName(),
                    leaveType.getMinDurationDays()
                ));
            }
        }

        // Validate maximum duration if specified
        if (leaveType.getMaxDurationDays() != null) {
            long requestedDays = java.time.temporal.ChronoUnit.DAYS.between(
                requestDto.getStartDate(),
                requestDto.getEndDate()
            ) + 1;
            if (requestedDays > leaveType.getMaxDurationDays()) {
                throw new RuntimeException(String.format(
                    "Maximum duration for %s is %d days",
                    leaveType.getName(),
                    leaveType.getMaxDurationDays()
                ));
            }
        }

        int overlappingLeaves = leaveRequestRepository.countOverlappingLeaves(
            employeeId,
            requestDto.getStartDate(),
            requestDto.getEndDate()
        );

        if (overlappingLeaves > 0) {
            throw new RuntimeException("You already have approved leave during this period");
        }

        // Check for duplicate leave requests (same employee, start date, and end date)
        int duplicateRequests = leaveRequestRepository.countDuplicateLeaveRequests(
            employeeId,
            requestDto.getStartDate(),
            requestDto.getEndDate()
        );

        if (duplicateRequests > 0) {
            throw new RuntimeException("You already have a leave request for the same period");
        }

        // Calculate total days automatically first
        long totalDays = java.time.temporal.ChronoUnit.DAYS.between(
            requestDto.getStartDate(),
            requestDto.getEndDate()
        ) + 1; // Include both start and end dates

        // Set total days in DTO for validation
        requestDto.setTotalDays((int) totalDays);

        validateLeaveBalance(employee, leaveType, (int) totalDays);

        LeaveRequest leaveRequest = new LeaveRequest();
        leaveRequest.setEmployee(employee);
        leaveRequest.setLeaveType(leaveType);
        leaveRequest.setStartDate(requestDto.getStartDate());
        leaveRequest.setEndDate(requestDto.getEndDate());
        leaveRequest.setTotalDays((int) totalDays);
        leaveRequest.setReason(requestDto.getReason());
        leaveRequest.setStatus(LeaveRequest.RequestStatus.PENDING);
        leaveRequest.setCreatedBy(employee);

        LeaveRequest savedRequest = leaveRequestRepository.save(leaveRequest);
        log.info("Leave request created for employee {} from {} to {} using leave type {}",
                employee.getEmployeeId(), requestDto.getStartDate(), requestDto.getEndDate(), leaveType.getName());

        return savedRequest;
    }

    @Transactional
    public LeaveRequest approveLeaveRequest(UUID uuid, Long supervisorId) {
        LeaveRequest leaveRequest = leaveRequestRepository.findByUuid(uuid)
            .orElseThrow(() -> new LeaveRequestException(LeaveRequestException.LeaveErrorType.NOT_FOUND));

        if (!leaveRequest.getStatus().equals(LeaveRequest.RequestStatus.PENDING)) {
            throw new LeaveRequestException(LeaveRequestException.LeaveErrorType.NOT_PENDING);
        }

        Employee supervisor = employeeRepository.findById(supervisorId)
            .orElseThrow(() -> new RuntimeException("Supervisor not found"));

        Employee employee = leaveRequest.getEmployee();
        if (!employee.getSupervisorId().equals(supervisorId)) {
            throw new LeaveRequestException(LeaveRequestException.LeaveErrorType.UNAUTHORIZED);
        }

        validateLeaveBalance(employee, leaveRequest.getLeaveType(), leaveRequest.getTotalDays());

        // Only deduct balance for leave types that have balance quota
        if (leaveRequest.getLeaveType() != null && leaveRequest.getLeaveType().getHasBalanceQuota()) {
            deductLeaveBalance(employee, leaveRequest);
        }

    
        leaveRequest.setStatus(LeaveRequest.RequestStatus.APPROVED);
        leaveRequest.setUpdatedBy(supervisor);

        LeaveRequest savedRequest = leaveRequestRepository.save(leaveRequest);
        log.info("Leave request {} approved by supervisor {}",
                uuid, supervisor.getEmployeeId());

        return savedRequest;
    }

    @Transactional
    public LeaveRequest rejectLeaveRequest(UUID uuid, Long supervisorId) {
        LeaveRequest leaveRequest = leaveRequestRepository.findByUuid(uuid)
            .orElseThrow(() -> new LeaveRequestException(LeaveRequestException.LeaveErrorType.NOT_FOUND));

        if (!leaveRequest.getStatus().equals(LeaveRequest.RequestStatus.PENDING)) {
            throw new LeaveRequestException(LeaveRequestException.LeaveErrorType.NOT_PENDING);
        }

        Employee supervisor = employeeRepository.findById(supervisorId)
            .orElseThrow(() -> new RuntimeException("Supervisor not found"));

        Employee employee = leaveRequest.getEmployee();
        if (!employee.getSupervisorId().equals(supervisorId)) {
            throw new RuntimeException("You are not authorized to reject this leave request");
        }

        leaveRequest.setStatus(LeaveRequest.RequestStatus.REJECTED);
        leaveRequest.setUpdatedBy(supervisor);

        LeaveRequest savedRequest = leaveRequestRepository.save(leaveRequest);
        log.info("Leave request {} rejected by supervisor {}",
                uuid, supervisor.getEmployeeId());

        return savedRequest;
    }

    @Transactional(readOnly = true)
    public List<LeaveRequest> getEmployeeLeaveRequests(Long employeeId) {
        return leaveRequestRepository.findByEmployeeIdOrderByCreatedAtDesc(employeeId);
    }

    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<LeaveRequest> getEmployeeLeaveRequests(Long employeeId, org.springframework.data.domain.Pageable pageable) {
        return leaveRequestRepository.findByEmployeeIdOrderByCreatedAtDesc(employeeId, pageable);
    }

    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<LeaveRequest> getEmployeeLeaveRequests(Long employeeId, org.springframework.data.domain.Pageable pageable, int days) {
        LocalDateTime startDate = LocalDate.now().minusDays(days).atStartOfDay();
        return leaveRequestRepository.findByEmployeeIdAndCreatedAtAfter(employeeId, startDate, pageable);
    }

    @Transactional(readOnly = true)
    public List<LeaveRequest> getPendingRequestsForSupervisor(Long supervisorId) {
        return leaveRequestRepository.findPendingRequestsBySupervisor(supervisorId);
    }

    @Transactional(readOnly = true)
    public Optional<LeaveRequest> getCurrentLeave(Long employeeId) {
        return leaveRequestRepository.findCurrentLeave(employeeId, LocalDate.now());
    }

    @Transactional(readOnly = true)
    public Optional<LeaveRequest> getLeaveRequestById(Long requestId) {
        return leaveRequestRepository.findById(requestId);
    }

    @Transactional(readOnly = true)
    public Optional<LeaveRequest> getLeaveRequestByUuid(UUID uuid) {
        return leaveRequestRepository.findByUuid(uuid);
    }

    private void validateLeaveBalance(Employee employee, LeaveType leaveType, int requestedDays) {
        // Only validate balance for leave types that have balance quota
        if (!leaveType.getHasBalanceQuota()) {
            return;
        }

        int availableBalance = getAvailableLeaveBalance(employee, leaveType);

        if (availableBalance < requestedDays) {
            throw new RuntimeException(String.format(
                "Insufficient leave balance for %s. Available: %d days, Requested: %d days",
                leaveType.getName(), availableBalance, requestedDays
            ));
        }
    }

    public int getAvailableLeaveBalance(Employee employee, LeaveType leaveType) {
        if (!leaveType.getHasBalanceQuota()) {
            return 0;
        }

        switch (leaveType.getCode()) {
            case "ANNUAL_LEAVE":
                return employee.getAnnualLeaveBalance();
            case "SICK_LEAVE":
                return employee.getSickLeaveBalance();
            default:
                return 0;
        }
    }

  
    private void deductLeaveBalance(Employee employee, LeaveRequest leaveRequest) {
        LeaveType leaveType = leaveRequest.getLeaveType();

        if (!leaveType.getHasBalanceQuota()) {
            return;
        }

        switch (leaveType.getCode()) {
            case "ANNUAL_LEAVE":
                employee.setAnnualLeaveBalance(
                    employee.getAnnualLeaveBalance() - leaveRequest.getTotalDays()
                );
                break;
            case "SICK_LEAVE":
                employee.setSickLeaveBalance(
                    employee.getSickLeaveBalance() - leaveRequest.getTotalDays()
                );
                break;
        }
        employeeRepository.save(employee);
    }

    }