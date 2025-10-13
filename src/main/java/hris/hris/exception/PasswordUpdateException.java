package hris.hris.exception;

public class PasswordUpdateException extends RuntimeException {

    public enum PasswordUpdateErrorType {
        USER_NOT_FOUND,
        INVALID_CURRENT_PASSWORD,
        PASSWORDS_DO_NOT_MATCH,
        SAME_PASSWORD_AS_CURRENT,
        VALIDATION_FAILED
    }

    private final PasswordUpdateErrorType errorType;
    private final String errorCode;

    public PasswordUpdateException(String message, PasswordUpdateErrorType errorType) {
        super(message);
        this.errorType = errorType;
        this.errorCode = generateErrorCode(errorType);
    }

    public PasswordUpdateException(String message, PasswordUpdateErrorType errorType, Throwable cause) {
        super(message, cause);
        this.errorType = errorType;
        this.errorCode = generateErrorCode(errorType);
    }

    private String generateErrorCode(PasswordUpdateErrorType errorType) {
        switch (errorType) {
            case USER_NOT_FOUND:
                return "USER_NOT_FOUND";
            case INVALID_CURRENT_PASSWORD:
                return "INVALID_CURRENT_PASSWORD";
            case PASSWORDS_DO_NOT_MATCH:
                return "PASSWORDS_DO_NOT_MATCH";
            case SAME_PASSWORD_AS_CURRENT:
                return "SAME_PASSWORD_AS_CURRENT";
            case VALIDATION_FAILED:
                return "VALIDATION_FAILED";
            default:
                return "PASSWORD_UPDATE_ERROR";
        }
    }

    public PasswordUpdateErrorType getErrorType() {
        return errorType;
    }

    public String getErrorCode() {
        return errorCode;
    }
}