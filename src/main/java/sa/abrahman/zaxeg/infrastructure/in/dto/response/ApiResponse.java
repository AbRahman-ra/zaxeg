package sa.abrahman.zaxeg.infrastructure.in.dto.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

public record ApiResponse<T>(
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime timestamp,
        int status, String message, T data) {
    public static <T> ApiResponse<T> from(int status, String message, T data) {
        return new ApiResponse<>(LocalDateTime.now(), status, message, data);
    }
}
