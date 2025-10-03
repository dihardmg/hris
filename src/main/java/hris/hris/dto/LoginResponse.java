package hris.hris.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    private String token;
    @Builder.Default
    private String type = "Bearer";
    private Long employeeId;
    private String employeeCode;
    private String firstName;
    private String lastName;
    private String email;
    private Date expiresAt;
    private String expiresAtWIB;
}