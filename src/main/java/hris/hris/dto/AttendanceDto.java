package hris.hris.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class AttendanceDto {
    private UUID uuid;
    private String employeeName;
    private LocalDate date;
    private LocalDateTime clockInTime;
    private LocalDateTime clockOutTime;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String locationAddress;
    private boolean isWithinGeofence;
    private BigDecimal faceRecognitionConfidence;
    private String notes;
}
