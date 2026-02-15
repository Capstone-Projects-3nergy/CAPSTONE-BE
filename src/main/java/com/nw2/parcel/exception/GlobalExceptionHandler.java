package com.nw2.parcel.exception;

import com.nw2.parcel.Dtos.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 404 - Resource Not Found
    @ExceptionHandler({ResourceNotFoundException.class, ParcelNotFoundException.class})
    public ResponseEntity<ApiError> handleNotFound(
            RuntimeException ex,
            HttpServletRequest request
    ) {
        return buildResponse(ex, request, HttpStatus.NOT_FOUND);
    }

    // 400 - Bad Request (Input ผิด)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleBadRequest(
            IllegalArgumentException ex,
            HttpServletRequest request
    ) {
        return buildResponse(ex, request, HttpStatus.BAD_REQUEST);
    }

    // 401 - Unauthorized
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiError> handleUnauthorized(
            UnauthorizedException ex,
            HttpServletRequest request
    ) {
        return buildResponse(ex, request, HttpStatus.UNAUTHORIZED);
    }

    // 409 - Conflict (Email ซ้ำ / state conflict)
    @ExceptionHandler({EmailAlreadyExistsException.class, ConflictException.class})
    public ResponseEntity<ApiError> handleConflict(
            RuntimeException ex,
            HttpServletRequest request
    ) {
        return buildResponse(ex, request, HttpStatus.CONFLICT);
    }

    // 500 - Real Server Error
    @ExceptionHandler({ExternalServiceException.class, Exception.class})
    public ResponseEntity<ApiError> handleServerError(
            Exception ex,
            HttpServletRequest request
    ) {

        ex.printStackTrace(); // 👈 เพิ่มบรรทัดนี้

        return buildResponse(
                new RuntimeException(ex.getMessage()),
                request,
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }

    private ResponseEntity<ApiError> buildResponse(
            RuntimeException ex,
            HttpServletRequest request,
            HttpStatus status
    ) {
        ApiError error = new ApiError(
                status.value(),
                status.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );
        return new ResponseEntity<>(error, status);
    }
}