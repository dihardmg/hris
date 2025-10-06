package hris.hris.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonInclude;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private T data;
    private String message;
    private String error;
    private Integer status;

    public static <T> ApiResponse<T> success(T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setData(data);
        return response;
    }

    public static <T> ApiResponse<T> success(T data, String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setData(data);
        response.setMessage(message);
        return response;
    }

    public static ApiResponse<Void> success(String message) {
        ApiResponse<Void> response = new ApiResponse<>();
        response.setMessage(message);
        return response;
    }

    public static <T> ApiResponse<T> error(String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setError(message);
        return response;
    }

    public static <T> ApiResponse<T> error(String message, int status) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setError(message);
        response.setStatus(status);
        return response;
    }

    public static ApiResponse<RateLimitResponse> error(RateLimitResponse data) {
        ApiResponse<RateLimitResponse> response = new ApiResponse<>();
        response.setData(data);
        response.setMessage(data.getMessage());
        return response;
    }
}