package hris.hris.dto;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = LeaveRequestValidator.class)
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidLeaveRequest {
    String message() default "Invalid leave request";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}