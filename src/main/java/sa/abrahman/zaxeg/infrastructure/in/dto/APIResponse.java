package sa.abrahman.zaxeg.infrastructure.in.dto;

import java.time.LocalDateTime;

public record APIResponse<T>(LocalDateTime timestamp, int status, String message, T data) {
    public static <T> APIResponse<T> from (int status, String message, T data) {
        return new APIResponse<>(LocalDateTime.now(), status, message, data);
    }
}
