package hris.hris.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonInclude;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LeaveTypeDto {

    private Long id;
    private String code;
    private String name;
    private String description;
    private Integer minDurationDays;
    private Boolean hasBalanceQuota;
    private Boolean isPaidLeave;
    private Integer maxDurationDays;
    private Boolean requiresDocument;
    private Boolean isActive;
}