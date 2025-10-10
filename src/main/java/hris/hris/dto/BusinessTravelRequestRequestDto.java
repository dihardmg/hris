package hris.hris.dto;

import lombok.Data;

@Data
public class BusinessTravelRequestRequestDto {

    private Long cityId;

    private String startDate;

    private String endDate;

    private String reason;

    // Convert to BusinessTravelRequestDto for service layer
    public BusinessTravelRequestDto toBusinessTravelRequestDto() {
        BusinessTravelRequestDto dto = new BusinessTravelRequestDto();
        dto.setCityId(this.cityId);
        dto.setStartDateFromString(this.startDate);
        dto.setEndDateFromString(this.endDate);
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