package hris.hris.exception;

public class AttendanceException extends BusinessException {

    public enum AttendanceErrorType {
        NOT_FOUND("DATA_NOT_FOUND", "Data not found");

        private final String code;
        private final String defaultMessage;

        AttendanceErrorType(String code, String defaultMessage) {
            this.code = code;
            this.defaultMessage = defaultMessage;
        }

        public String getCode() {
            return code;
        }

        public String getDefaultMessage() {
            return defaultMessage;
        }
    }

    private final AttendanceErrorType errorType;
    private final Object details;

    public AttendanceException(AttendanceErrorType errorType) {
        super(errorType.getDefaultMessage(), errorType.getCode());
        this.errorType = errorType;
        this.details = null;
    }

    public AttendanceException(AttendanceErrorType errorType, String customMessage) {
        super(customMessage, errorType.getCode());
        this.errorType = errorType;
        this.details = null;
    }

    public AttendanceException(AttendanceErrorType errorType, String customMessage, Object details) {
        super(customMessage, errorType.getCode(), String.valueOf(details));
        this.errorType = errorType;
        this.details = details;
    }

    public AttendanceErrorType getErrorType() {
        return errorType;
    }

    public String getDetails() {
        return details != null ? String.valueOf(details) : null;
    }
}
