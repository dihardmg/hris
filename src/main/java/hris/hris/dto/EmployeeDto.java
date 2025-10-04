package hris.hris.dto;

import lombok.Data;

@Data
public class EmployeeDto {
    private Long id;
    private String employeeCode;
    private String firstName;
    private String lastName;
    private String email;
}