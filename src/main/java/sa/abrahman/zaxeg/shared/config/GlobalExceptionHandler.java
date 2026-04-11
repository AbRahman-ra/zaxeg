package sa.abrahman.zaxeg.shared.config;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import sa.abrahman.zaxeg.adapter.generate.out.exception.XmlGenerationException;
import sa.abrahman.zaxeg.core.generate.domain.exception.InvoiceRuleViolationException;
import sa.abrahman.zaxeg.shared.dto.ApiResponse;
import tools.jackson.databind.exc.InvalidFormatException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // DTO Schema Validation (Missing fields, bad formatting)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationExceptions(MethodArgumentNotValidException e) {
        Map<String, String> validationErrors = new HashMap<>();
        String errorTitle = "Bad Request";

        e.getBindingResult().getAllErrors().forEach(error -> {
            String field = ((FieldError) error).getField();
            String msg = error.getDefaultMessage();
            validationErrors.put(field, msg);
        });

        return buildResponse(HttpStatus.BAD_REQUEST, errorTitle, validationErrors);
    }

    // DTO Schema parsing (Malforms payloads and parsing errors)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<String>> handleHttpMessageNotReadable(HttpMessageNotReadableException e) {
        String errorMessage = "Malformed JSON request. Please check your payload structure.";

        // if Jackson Parsing error
        if (e.getCause() instanceof InvalidFormatException invalidFormatException) {
            boolean isEnum = Optional.ofNullable(invalidFormatException.getTargetType()).map(Class::isEnum).orElse(false);
            if (isEnum) {
                String invalidValue = invalidFormatException.getValue().toString();
                String fieldName = invalidFormatException.getPath().get(invalidFormatException.getPath().size() - 1).getPropertyName();

                // Dynamically grab all accepted values from the Enum class!
                Object[] enumConstants = invalidFormatException.getTargetType().getEnumConstants();
                String acceptedValues = Arrays.toString(enumConstants);

                errorMessage = String.format("Invalid value '%s' for field '%s'. Accepted values are: %s",
                        invalidValue, fieldName, acceptedValues);
            }
        }

        return buildResponse(HttpStatus.BAD_REQUEST, "Invalid Payload Format", errorMessage);
    }

    // Infrastructure Failures (XML Formatting)
    @ExceptionHandler(XmlGenerationException.class)
    public ResponseEntity<ApiResponse<String>> handleXmlGenerationException(XmlGenerationException e) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error",
                "An unexpected error occurred while generating the UBL 2.1 document.");
    }

    // UBL & ZATCA Business Rule Violations
    @ExceptionHandler(InvoiceRuleViolationException.class)
    public ResponseEntity<ApiResponse<String>> handleInvoiceRuleViolation(InvoiceRuleViolationException e) {
        return buildResponse(HttpStatus.UNPROCESSABLE_CONTENT, "ZATCA Rule Violation", e.getMessage());
    }

    // Domain State Violations (e.g., Missing lines for financial calculation)
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponse<String>> handleIllegalStateException(IllegalStateException e) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Invalid Document State", e.getMessage());
    }

    // General Other Exceptions
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<String>> handleGenericException(Exception e) {
        // MUST log the stack trace here so you can fix bugs!
        e.printStackTrace();

        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "System Error",
                "An unexpected system error occurred.");
    }

    // ============ HELPER METHOD ============
    private <T> ResponseEntity<ApiResponse<T>> buildResponse(HttpStatus status, String errorTitle, T details) {
        ApiResponse<T> response = new ApiResponse<>(LocalDateTime.now(), status.value(), errorTitle, details);
        return ResponseEntity.status(status).body(response);
    }
}
