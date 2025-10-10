package hris.hris.dto;

import hris.hris.model.LeaveRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.BeanWrapperImpl;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

public class LeaveRequestValidator implements ConstraintValidator<ValidLeaveRequest, LeaveRequestDto> {

    private static final Pattern DATE_PATTERN = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}$");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public void initialize(ValidLeaveRequest constraintAnnotation) {
    }

    @Override
    public boolean isValid(LeaveRequestDto leaveRequestDto, ConstraintValidatorContext context) {
        boolean isValid = true;

        // Disable default violation message
        context.disableDefaultConstraintViolation();

        // Validate leaveTypeId is not null and positive
        if (leaveRequestDto.getLeaveTypeId() == null) {
            context.buildConstraintViolationWithTemplate("must be not null")
                   .addPropertyNode("leaveTypeId")
                   .addConstraintViolation();
            isValid = false;
        } else if (leaveRequestDto.getLeaveTypeId() <= 0) {
            context.buildConstraintViolationWithTemplate("only number int")
                   .addPropertyNode("leaveTypeId")
                   .addConstraintViolation();
            isValid = false;
        }

        // Validate startDate
        if (leaveRequestDto.getStartDate() == null) {
            context.buildConstraintViolationWithTemplate("must be not null")
                   .addPropertyNode("startDate")
                   .addConstraintViolation();
            isValid = false;
        } else {
            // Check if startDate is in the past
            if (leaveRequestDto.getStartDate().isBefore(LocalDate.now())) {
                context.buildConstraintViolationWithTemplate("must be today or future date")
                       .addPropertyNode("startDate")
                       .addConstraintViolation();
                isValid = false;
            }

            // Format validation - LocalDate yang valid selalu dalam format yyyy-MM-dd
            // Tapi kita tambahkan validasi untuk tahun yang masuk akal
            if (leaveRequestDto.getStartDate().getYear() < 2000 || leaveRequestDto.getStartDate().getYear() > 2100) {
                context.buildConstraintViolationWithTemplate("format YYYY-MM-DD")
                       .addPropertyNode("startDate")
                       .addConstraintViolation();
                isValid = false;
            }
        }

        // Validate endDate
        if (leaveRequestDto.getEndDate() == null) {
            context.buildConstraintViolationWithTemplate("must be not null")
                   .addPropertyNode("endDate")
                   .addConstraintViolation();
            isValid = false;
        } else {
            // Check if endDate is in the past
            if (leaveRequestDto.getEndDate().isBefore(LocalDate.now())) {
                context.buildConstraintViolationWithTemplate("must be today or future date")
                       .addPropertyNode("endDate")
                       .addConstraintViolation();
                isValid = false;
            }

            // Format validation
            if (leaveRequestDto.getEndDate().getYear() < 2000 || leaveRequestDto.getEndDate().getYear() > 2100) {
                context.buildConstraintViolationWithTemplate("format YYYY-MM-DD")
                       .addPropertyNode("endDate")
                       .addConstraintViolation();
                isValid = false;
            }
        }

        // Validate date range logic if both dates are present
        if (leaveRequestDto.getStartDate() != null && leaveRequestDto.getEndDate() != null) {
            if (leaveRequestDto.getEndDate().isBefore(leaveRequestDto.getStartDate())) {
                context.buildConstraintViolationWithTemplate("must be after start date")
                       .addPropertyNode("endDate")
                       .addConstraintViolation();
                isValid = false;
            }

            // Check if date range is too long (more than 365 days)
            long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(
                leaveRequestDto.getStartDate(), leaveRequestDto.getEndDate());
            if (daysBetween > 365) {
                context.buildConstraintViolationWithTemplate("leave period cannot exceed 365 days")
                       .addPropertyNode("endDate")
                       .addConstraintViolation();
                isValid = false;
            }
        }

        return isValid;
    }
}