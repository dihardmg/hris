package hris.hris.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class AttendanceDto {
    private Long id;
    private Long employeeId;
    private String employeeName;
    private LocalDateTime clockInTime;
    private LocalDateTime clockOutTime;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String locationAddress;
    private boolean isWithinGeofence;
    private BigDecimal faceRecognitionConfidence;
    private String notes;
}
