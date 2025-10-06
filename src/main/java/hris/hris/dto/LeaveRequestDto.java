package hris.hris.dto;

import hris.hris.model.LeaveRequest;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalDate;

@Data
public class LeaveRequestDto {

    @NotNull(message = "Leave type ID is required")
    private Long leaveTypeId;

    // For backward compatibility during migration
    private LeaveRequest.LeaveTypeEnum leaveTypeEnum;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    private LocalDate endDate;

    private Integer totalDays;

    private String reason;
}