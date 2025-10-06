package hris.hris.dto;

import hris.hris.model.BusinessTravelRequest;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class BusinessTravelRequestResponseDto {
    private UUID uuid;
    private Long employeeId;
    private String employeeName;
    private CityResponseDto city;
    private String startDate;
    private String endDate;
    private Integer totalDays;
    private String reason;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private EmployeeDto createdById;
    private EmployeeDto updatedById;

    public static BusinessTravelRequestResponseDto fromBusinessTravelRequest(BusinessTravelRequest request) {
        BusinessTravelRequestResponseDto dto = new BusinessTravelRequestResponseDto();
        dto.setUuid(request.getUuid());
        dto.setEmployeeId(request.getEmployeeId());
        if (request.getEmployee() != null) {
            dto.setEmployeeName(request.getEmployee().getFirstName() + " " + request.getEmployee().getLastName());
        } else {
            dto.setEmployeeName(null);
        }
        if (request.getCity() != null) {
            CityResponseDto cityDto = new CityResponseDto();
            cityDto.setId(request.getCity().getId());
            cityDto.setCityName(request.getCity().getCityName());
            cityDto.setProvinceName(request.getCity().getProvinceName());
            cityDto.setCityDisplayName(request.getCity().getCityName() + ", " + request.getCity().getProvinceName());
            dto.setCity(cityDto);
        } else {
            dto.setCity(null);
        }
        dto.setStartDate(request.getStartDate().toString());
        dto.setEndDate(request.getEndDate().toString());
        dto.setTotalDays(request.getTotalDays());
        dto.setReason(request.getReason());
        dto.setStatus(request.getStatus().toString());
        dto.setCreatedAt(request.getCreatedAt());
        dto.setUpdatedAt(request.getUpdatedAt());

        // Audit fields - detailed employee objects
        if (request.getCreatedBy() != null) {
            EmployeeDto createdByDto = new EmployeeDto();
            createdByDto.setId(request.getCreatedBy().getId());
            createdByDto.setEmployeeCode(request.getCreatedBy().getEmployeeId());
            createdByDto.setFirstName(request.getCreatedBy().getFirstName());
            createdByDto.setLastName(request.getCreatedBy().getLastName());
            createdByDto.setEmail(request.getCreatedBy().getEmail());
            dto.setCreatedById(createdByDto);
        } else {
            dto.setCreatedById(null);
        }

        if (request.getUpdatedBy() != null) {
            EmployeeDto updatedByDto = new EmployeeDto();
            updatedByDto.setId(request.getUpdatedBy().getId());
            updatedByDto.setEmployeeCode(request.getUpdatedBy().getEmployeeId());
            updatedByDto.setFirstName(request.getUpdatedBy().getFirstName());
            updatedByDto.setLastName(request.getUpdatedBy().getLastName());
            updatedByDto.setEmail(request.getUpdatedBy().getEmail());
            dto.setUpdatedById(updatedByDto);
        } else {
            dto.setUpdatedById(null);
        }

        return dto;
    }
}