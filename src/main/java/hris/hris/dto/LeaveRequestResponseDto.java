package hris.hris.dto;

import hris.hris.model.LeaveRequest;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class LeaveRequestResponseDto {
    private UUID uuid;
    private LeaveTypeDto leaveType;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer totalDays;
    private String reason;
    private LeaveRequest.RequestStatus status;
    private LocalDateTime submissionDate;
    private LocalDateTime updatedAt;
    private Integer remainingBalance;
    private UserInfo createdBy;
    private UserInfo updatedBy;

    @Data
    public static class UserInfo {
        private Long id;
        private String employeeCode;
        private String firstName;
        private String lastName;
        private String email;
    }

    public static LeaveRequestResponseDto fromLeaveRequest(LeaveRequest leaveRequest, Integer remainingBalanceOverride) {
        LeaveRequestResponseDto dto = new LeaveRequestResponseDto();
        dto.setUuid(leaveRequest.getUuid());
        // Set leave type information
        LeaveTypeDto leaveTypeDto = new LeaveTypeDto();
        leaveTypeDto.setId(leaveRequest.getLeaveType().getId());
        leaveTypeDto.setCode(leaveRequest.getLeaveType().getCode());
        leaveTypeDto.setName(leaveRequest.getLeaveType().getName());
        leaveTypeDto.setHasBalanceQuota(leaveRequest.getLeaveType().getHasBalanceQuota());
        dto.setLeaveType(leaveTypeDto);
        dto.setStartDate(leaveRequest.getStartDate());
        dto.setEndDate(leaveRequest.getEndDate());
        dto.setTotalDays(leaveRequest.getTotalDays());
        dto.setReason(leaveRequest.getReason());
        dto.setStatus(leaveRequest.getStatus());
        dto.setSubmissionDate(leaveRequest.getCreatedAt());
        dto.setUpdatedAt(leaveRequest.getUpdatedAt());
        // Use override if provided, otherwise calculate it dynamically
        dto.setRemainingBalance(remainingBalanceOverride != null ? remainingBalanceOverride : 0);

        // Add created by information if present
        if (leaveRequest.getCreatedBy() != null) {
            UserInfo createdByInfo = new UserInfo();
            createdByInfo.setId(leaveRequest.getCreatedBy().getId());
            createdByInfo.setEmployeeCode(leaveRequest.getCreatedBy().getEmployeeId());
            createdByInfo.setFirstName(leaveRequest.getCreatedBy().getFirstName());
            createdByInfo.setLastName(leaveRequest.getCreatedBy().getLastName());
            createdByInfo.setEmail(leaveRequest.getCreatedBy().getEmail());
            dto.setCreatedBy(createdByInfo);
        }

        // Add updated by information if present
        if (leaveRequest.getUpdatedBy() != null) {
            UserInfo updatedByInfo = new UserInfo();
            updatedByInfo.setId(leaveRequest.getUpdatedBy().getId());
            updatedByInfo.setEmployeeCode(leaveRequest.getUpdatedBy().getEmployeeId());
            updatedByInfo.setFirstName(leaveRequest.getUpdatedBy().getFirstName());
            updatedByInfo.setLastName(leaveRequest.getUpdatedBy().getLastName());
            updatedByInfo.setEmail(leaveRequest.getUpdatedBy().getEmail());
            dto.setUpdatedBy(updatedByInfo);
        }

        return dto;
    }
}