package hris.hris.dto;

import lombok.Data;

@Data
public class LeaveRequestRequestDto {

    private Long leaveTypeId;

    private String startDate;

    private String endDate;

    private Integer totalDays;

    private String reason;

    // Convert to LeaveRequestDto for service layer
    public LeaveRequestDto toLeaveRequestDto() {
        LeaveRequestDto dto = new LeaveRequestDto();
        dto.setLeaveTypeId(this.leaveTypeId);
        dto.setStartDateFromString(this.startDate);
        dto.setEndDateFromString(this.endDate);
        dto.setTotalDays(this.totalDays);
        dto.setReason(this.reason);
        return dto;
    }

    // Helper method to convert String to LocalDate
    public java.time.LocalDate getStartDateAsDate() {
        if (startDate == null || startDate.trim().isEmpty()) {
            return null;
        }
        try {
            return java.time.LocalDate.parse(startDate);
        } catch (Exception e) {
            return null;
        }
    }

    // Helper method to convert String to LocalDate
    public java.time.LocalDate getEndDateAsDate() {
        if (endDate == null || endDate.trim().isEmpty()) {
            return null;
        }
        try {
            return java.time.LocalDate.parse(endDate);
        } catch (Exception e) {
            return null;
        }
    }
}