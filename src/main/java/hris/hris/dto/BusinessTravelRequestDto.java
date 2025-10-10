package hris.hris.dto;

import hris.hris.model.BusinessTravelRequest;
import lombok.Data;

import java.time.LocalDate;

@Data
public class BusinessTravelRequestDto {

    private Long cityId;

    private LocalDate startDate;

    private LocalDate endDate;

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