package hris.hris.exception;

public class BusinessTravelRequestException extends BusinessException {

    public enum BusinessTravelErrorType {
        NOT_FOUND("DATA_NOT_FOUND", "Business travel request not found"),
        NOT_PENDING("BUSINESS_TRAVEL_NOT_PENDING", "Business travel request is not in pending status"),
        ALREADY_APPROVED("BUSINESS_TRAVEL_ALREADY_APPROVED", "Business travel request has already been approved"),
        ALREADY_REJECTED("BUSINESS_TRAVEL_ALREADY_REJECTED", "Business travel request has already been rejected"),
        UNAUTHORIZED("UNAUTHORIZED_APPROVAL", "You are not authorized to approve this business travel request"),
        OVERLAPPING_DATES("OVERLAPPING_TRAVEL_DATES", "Business travel dates overlap with existing travel");

        private final String code;
        private final String defaultMessage;

        BusinessTravelErrorType(String code, String defaultMessage) {
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

    private final BusinessTravelErrorType errorType;
    private final Object details;

    public BusinessTravelRequestException(BusinessTravelErrorType errorType) {
        super(errorType.getDefaultMessage(), errorType.getCode());
        this.errorType = errorType;
        this.details = null;
    }

    public BusinessTravelRequestException(BusinessTravelErrorType errorType, String customMessage) {
        super(customMessage, errorType.getCode());
        this.errorType = errorType;
        this.details = null;
    }

    public BusinessTravelRequestException(BusinessTravelErrorType errorType, String customMessage, Object details) {
        super(customMessage, errorType.getCode(), String.valueOf(details));
        this.errorType = errorType;
        this.details = details;
    }

    public BusinessTravelErrorType getErrorType() {
        return errorType;
    }

    public String getDetails() {
        return details != null ? String.valueOf(details) : null;
    }
}