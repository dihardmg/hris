package hris.hris.exception;

public class LeaveRequestException extends BusinessException {

    public enum LeaveErrorType {
        NOT_FOUND("DATA_NOT_FOUND", "Leave request not found"),
        NOT_PENDING("LEAVE_NOT_PENDING", "Leave request is not in pending status"),
        ALREADY_APPROVED("LEAVE_ALREADY_APPROVED", "Leave request has already been approved"),
        ALREADY_REJECTED("LEAVE_ALREADY_REJECTED", "Leave request has already been rejected"),
        UNAUTHORIZED("UNAUTHORIZED_APPROVAL", "You are not authorized to approve this leave request"),
        INSUFFICIENT_BALANCE("INSUFFICIENT_LEAVE_BALANCE", "Insufficient leave balance"),
        OVERLAPPING_DATES("OVERLAPPING_LEAVE_DATES", "Leave dates overlap with existing leave");

        private final String code;
        private final String defaultMessage;

        LeaveErrorType(String code, String defaultMessage) {
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

    private final LeaveErrorType errorType;
    private final Object details;

    public LeaveRequestException(LeaveErrorType errorType) {
        super(errorType.getDefaultMessage(), errorType.getCode());
        this.errorType = errorType;
        this.details = null;
    }

    public LeaveRequestException(LeaveErrorType errorType, String customMessage) {
        super(customMessage, errorType.getCode());
        this.errorType = errorType;
        this.details = null;
    }

    public LeaveRequestException(LeaveErrorType errorType, String customMessage, Object details) {
        super(customMessage, errorType.getCode(), String.valueOf(details));
        this.errorType = errorType;
        this.details = details;
    }

    public LeaveErrorType getErrorType() {
        return errorType;
    }

    public String getDetails() {
        return details != null ? String.valueOf(details) : null;
    }
}