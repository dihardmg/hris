package hris.hris.service;

import hris.hris.dto.LeaveBalanceDto;
import hris.hris.model.Employee;
import hris.hris.model.LeaveRequest;
import hris.hris.model.LeaveType;
import hris.hris.repository.EmployeeRepository;
import hris.hris.repository.LeaveRequestRepository;
import hris.hris.repository.LeaveTypeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class LeaveBalanceService {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private LeaveTypeRepository leaveTypeRepository;

    @Autowired
    private LeaveRequestRepository leaveRequestRepository;

    @Transactional(readOnly = true)
    public List<LeaveBalanceDto> getAllLeaveBalancesForEmployee(Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
            .orElseThrow(() -> new RuntimeException("Employee not found"));

        List<LeaveType> allLeaveTypes = leaveTypeRepository.findByIsActiveTrue();

        return allLeaveTypes.stream()
            .map(leaveType -> calculateLeaveBalance(employee, leaveType))
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<LeaveBalanceDto> getLeaveBalanceForEmployeeAndType(Long employeeId, Long leaveTypeId) {
        Employee employee = employeeRepository.findById(employeeId)
            .orElseThrow(() -> new RuntimeException("Employee not found"));

        LeaveType leaveType = leaveTypeRepository.findById(leaveTypeId)
            .orElse(null);

        if (leaveType == null || !leaveType.getIsActive()) {
            return Optional.empty();
        }

        return Optional.of(calculateLeaveBalance(employee, leaveType));
    }

    @Transactional(readOnly = true)
    public List<LeaveBalanceDto> getLeaveBalancesForSupervisor(Long supervisorId) {
        List<Employee> subordinates = employeeRepository.findBySupervisorId(supervisorId);

        return subordinates.stream()
            .flatMap(employee -> leaveTypeRepository.findByIsActiveTrue().stream()
                .map(leaveType -> calculateLeaveBalance(employee, leaveType)))
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<LeaveBalanceDto> getLeaveBalancesForManager() {
        List<Employee> allEmployees = employeeRepository.findByIsActiveTrue();

        return allEmployees.stream()
            .flatMap(employee -> leaveTypeRepository.findByIsActiveTrue().stream()
                .map(leaveType -> calculateLeaveBalance(employee, leaveType)))
            .collect(Collectors.toList());
    }

    private LeaveBalanceDto calculateLeaveBalance(Employee employee, LeaveType leaveType) {
        LeaveBalanceDto dto = new LeaveBalanceDto();
        dto.setEmployeeId(employee.getId());
        dto.setEmployeeName(employee.getFirstName() + " " + employee.getLastName());
        dto.setEmployeeEmail(employee.getEmail());
        dto.setLeaveTypeId(leaveType.getId());
        dto.setLeaveTypeCode(leaveType.getCode());
        dto.setLeaveTypeName(leaveType.getName());
        dto.setHasBalanceQuota(leaveType.getHasBalanceQuota());

        if (!leaveType.getHasBalanceQuota()) {
            dto.setTotalQuota(0);
            dto.setUsedQuota(0);
            dto.setRemainingQuota(0);
            dto.setPercentageUsed(BigDecimal.ZERO);
        } else {
            Integer totalQuota = getTotalQuotaForLeaveType(employee, leaveType);
            Integer usedQuota = getUsedQuotaForLeaveType(employee, leaveType);
            Integer remainingQuota = totalQuota - usedQuota;

            dto.setTotalQuota(totalQuota);
            dto.setUsedQuota(usedQuota);
            dto.setRemainingQuota(remainingQuota);

            if (totalQuota > 0) {
                BigDecimal percentageUsed = new BigDecimal(usedQuota)
                    .multiply(new BigDecimal("100"))
                    .divide(new BigDecimal(totalQuota), 2, RoundingMode.HALF_UP);
                dto.setPercentageUsed(percentageUsed);
            } else {
                dto.setPercentageUsed(BigDecimal.ZERO);
            }
        }

        return dto;
    }

    private Integer getTotalQuotaForLeaveType(Employee employee, LeaveType leaveType) {
        switch (leaveType.getCode()) {
            case "ANNUAL_LEAVE":
                return employee.getAnnualLeaveBalance();
            case "SICK_LEAVE":
                return employee.getSickLeaveBalance();
            default:
                return 0;
        }
    }

    private Integer getUsedQuotaForLeaveType(Employee employee, LeaveType leaveType) {
        if (!leaveType.getHasBalanceQuota()) {
            return 0;
        }

        List<LeaveRequest> approvedLeaves = leaveRequestRepository
            .findByEmployeeIdAndLeaveTypeCodeAndStatus(
                employee.getId(),
                leaveType.getCode(),
                LeaveRequest.RequestStatus.APPROVED
            );

        return approvedLeaves.stream()
            .mapToInt(LeaveRequest::getTotalDays)
            .sum();
    }
}