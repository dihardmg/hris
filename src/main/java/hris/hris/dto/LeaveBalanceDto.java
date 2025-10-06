package hris.hris.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeaveBalanceDto {

    private Long employeeId;
    private String employeeName;
    private String employeeEmail;
    private Long leaveTypeId;
    private String leaveTypeCode;
    private String leaveTypeName;
    private Integer totalQuota;
    private Integer usedQuota;
    private Integer remainingQuota;
    private Boolean hasBalanceQuota;
    private BigDecimal percentageUsed;
}