package hris.hris.exception;

import java.util.HashMap;
import java.util.Map;

public class LeaveRequestValidationException extends RuntimeException {
    private Map<String, Object> errors;

    public LeaveRequestValidationException(Map<String, Object> errors) {
        super("Validation failed");
        this.errors = errors;
    }

    public LeaveRequestValidationException(String field, String message) {
        super("Validation failed");
        this.errors = new HashMap<>();
        this.errors.put(field, new String[]{message});
    }

    public Map<String, Object> getErrors() {
        return errors;
    }
}