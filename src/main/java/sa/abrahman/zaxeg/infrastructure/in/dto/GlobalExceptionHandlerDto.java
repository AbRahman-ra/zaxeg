package sa.abrahman.zaxeg.infrastructure.in.dto;

import java.time.LocalDateTime;

public record GlobalExceptionHandlerDto(LocalDateTime timestamp, int status, String error, Object details) {}
