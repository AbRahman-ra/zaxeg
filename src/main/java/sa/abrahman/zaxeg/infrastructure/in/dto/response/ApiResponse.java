package sa.abrahman.zaxeg.infrastructure.in.dto.response;

import java.time.LocalDateTime;

public record ApiResponse<T>(LocalDateTime timestamp, int status, String message, T data) {
    public static <T> ApiResponse<T> from (int status, String message, T data) {
        return new ApiResponse<>(LocalDateTime.now(), status, message, data);
    }
}
