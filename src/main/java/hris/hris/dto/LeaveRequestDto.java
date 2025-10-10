package hris.hris.dto;

import hris.hris.model.LeaveRequest;
import lombok.Data;

import java.time.LocalDate;

@Data
public class LeaveRequestDto {

    private Long leaveTypeId;

    // For backward compatibility during migration
    private LeaveRequest.LeaveTypeEnum leaveTypeEnum;

    private LocalDate startDate;

    private LocalDate endDate;

    private Integer totalDays;

    private String reason;

    // Helper method to convert String to LocalDate
    public LocalDate getStartDateAsDate() {
        return startDate;
    }

    // Helper method to convert String to LocalDate
    public LocalDate getEndDateAsDate() {
        return endDate;
    }

    // Helper method to set from string
    public void setStartDateFromString(String startDateStr) {
        if (startDateStr != null && !startDateStr.trim().isEmpty()) {
            try {
                this.startDate = LocalDate.parse(startDateStr);
            } catch (Exception e) {
                this.startDate = null;
            }
        }
    }

    // Helper method to set from string
    public void setEndDateFromString(String endDateStr) {
        if (endDateStr != null && !endDateStr.trim().isEmpty()) {
            try {
                this.endDate = LocalDate.parse(endDateStr);
            } catch (Exception e) {
                this.endDate = null;
            }
        }
    }
}