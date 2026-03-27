package com.fatihozkurt.fatihozkurtcom.common.exception;

import com.fatihozkurt.fatihozkurtcom.common.api.ApiErrorResponse;
import com.fatihozkurt.fatihozkurtcom.i18n.LocalizedMessageService;
import jakarta.validation.ConstraintViolationException;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;

/**
 * Handles API exceptions and maps them into standard error payloads.
 */
@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final LocalizedMessageService localizedMessageService;

    /**
     * Handles business exceptions with known error codes.
     *
     * @param ex exception
     * @param request request
     * @return mapped response
     */
    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiErrorResponse> handleAppException(AppException ex, ServletWebRequest request) {
        String message = localizedMessageService.get(ex.getErrorCode().messageKey(), ex.getArgs());
        log.warn("Handled application error code={} path={} message={}", ex.getErrorCode().name(), request.getRequest().getRequestURI(), message);
        return ResponseEntity.status(ex.getErrorCode().status()).body(
                new ApiErrorResponse(
                        ex.getErrorCode().name(),
                        message,
                        request.getRequest().getRequestURI(),
                        OffsetDateTime.now(),
                        List.of()
                )
        );
    }

    /**
     * Handles bean validation failures.
     *
     * @param ex exception
     * @param request request
     * @return mapped response
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex, ServletWebRequest request) {
        List<ApiErrorResponse.FieldErrorItem> fieldErrors = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(this::toFieldErrorItem)
                .toList();
        log.warn("Validation error path={} errors={}", request.getRequest().getRequestURI(), fieldErrors.size());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                new ApiErrorResponse(
                        ErrorCode.VAL001.name(),
                        localizedMessageService.get(ErrorCode.VAL001.messageKey()),
                        request.getRequest().getRequestURI(),
                        OffsetDateTime.now(),
                        fieldErrors
                )
        );
    }

    /**
     * Handles constraint validation failures.
     *
     * @param ex exception
     * @param request request
     * @return mapped response
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolationException(ConstraintViolationException ex, ServletWebRequest request) {
        List<ApiErrorResponse.FieldErrorItem> fieldErrors = ex.getConstraintViolations().stream()
                .map(violation -> new ApiErrorResponse.FieldErrorItem(violation.getPropertyPath().toString(), violation.getMessage()))
                .toList();
        log.warn("Constraint violation path={} errors={}", request.getRequest().getRequestURI(), fieldErrors.size());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                new ApiErrorResponse(
                        ErrorCode.VAL001.name(),
                        localizedMessageService.get(ErrorCode.VAL001.messageKey()),
                        request.getRequest().getRequestURI(),
                        OffsetDateTime.now(),
                        fieldErrors
                )
        );
    }

    /**
     * Handles malformed or incompatible JSON payloads.
     *
     * @param ex exception
     * @param request request
     * @return mapped response
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex, ServletWebRequest request) {
        log.warn("Malformed payload path={} message={}", request.getRequest().getRequestURI(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                new ApiErrorResponse(
                        ErrorCode.VAL001.name(),
                        localizedMessageService.get(ErrorCode.VAL001.messageKey()),
                        request.getRequest().getRequestURI(),
                        OffsetDateTime.now(),
                        List.of()
                )
        );
    }

    /**
     * Handles unexpected exceptions.
     *
     * @param ex exception
     * @param request request
     * @return mapped response
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleException(Exception ex, ServletWebRequest request) {
        log.error("Unhandled exception path={} message={}", request.getRequest().getRequestURI(), ex.getMessage(), ex);
        return ResponseEntity.status(ErrorCode.SYS001.status()).body(
                new ApiErrorResponse(
                        ErrorCode.SYS001.name(),
                        localizedMessageService.get(ErrorCode.SYS001.messageKey()),
                        request.getRequest().getRequestURI(),
                        OffsetDateTime.now(),
                        List.of()
                )
        );
    }

    private ApiErrorResponse.FieldErrorItem toFieldErrorItem(FieldError fieldError) {
        return new ApiErrorResponse.FieldErrorItem(fieldError.getField(), fieldError.getDefaultMessage());
    }
}
