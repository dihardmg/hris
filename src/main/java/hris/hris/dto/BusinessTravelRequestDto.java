package hris.hris.dto;

import hris.hris.model.BusinessTravelRequest;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class BusinessTravelRequestDto {

    @NotBlank(message = "Travel purpose is required")
    private String travelPurpose;

    @NotBlank(message = "Destination is required")
    private String destination;

    @NotNull(message = "Start date is required")
    @Future(message = "Start date must be in the future")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    @Future(message = "End date must be in the future")
    private LocalDate endDate;

    private BigDecimal estimatedCost;

    @NotNull(message = "Transportation type is required")
    private BusinessTravelRequest.TransportationType transportationType;

    private Boolean accommodationRequired = false;
}