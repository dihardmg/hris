package hris.hris.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
    private String error;
    private String message;
    private String path;
    private int status;
    private LocalDateTime timestamp;
    private String details;

    public static ErrorResponse notFound(String message, String path) {
        ErrorResponse response = new ErrorResponse();
        response.setError("NOT_FOUND");
        response.setMessage(message);
        response.setPath(path);
        response.setStatus(404);
        response.setTimestamp(LocalDateTime.now());
        return response;
    }

    public static ErrorResponse forbidden(String message, String path) {
        ErrorResponse response = new ErrorResponse();
        response.setError("FORBIDDEN");
        response.setMessage(message);
        response.setPath(path);
        response.setStatus(403);
        response.setTimestamp(LocalDateTime.now());
        return response;
    }

    public static ErrorResponse badRequest(String message, String path, String details) {
        ErrorResponse response = new ErrorResponse();
        response.setError("BAD_REQUEST");
        response.setMessage(message);
        response.setPath(path);
        response.setStatus(400);
        response.setTimestamp(LocalDateTime.now());
        response.setDetails(details);
        return response;
    }
}