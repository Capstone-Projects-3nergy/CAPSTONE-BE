package com.nw2.parcel.exception;

import com.nw2.parcel.Dtos.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // ===============================
    // 404 - Resource Not Found
    // ===============================
    @ExceptionHandler({ResourceNotFoundException.class, ParcelNotFoundException.class})
    public ResponseEntity<ApiError> handleNotFound(
            RuntimeException ex,
            HttpServletRequest request
    ) {
        return buildResponse(ex, request, HttpStatus.NOT_FOUND);
    }

    // ===============================
    // 400 - Bad Request (Input ผิด)
    // ===============================
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleBadRequest(
            IllegalArgumentException ex,
            HttpServletRequest request
    ) {
        return buildResponse(ex, request, HttpStatus.BAD_REQUEST);
    }

    // ===============================
    // 401 - Unauthorized
    // ===============================
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiError> handleUnauthorized(
            UnauthorizedException ex,
            HttpServletRequest request
    ) {
        return buildResponse(ex, request, HttpStatus.UNAUTHORIZED);
    }

    // ===============================
    // 409 - Conflict (Email ซ้ำ / state conflict)
    // ===============================
    @ExceptionHandler({EmailAlreadyExistsException.class, ConflictException.class})
    public ResponseEntity<ApiError> handleConflict(
            RuntimeException ex,
            HttpServletRequest request
    ) {
        return buildResponse(ex, request, HttpStatus.CONFLICT);
    }

    // ===============================
    // 500 - Real Server Error
    // ===============================
    @ExceptionHandler({ExternalServiceException.class, Exception.class})
    public ResponseEntity<ApiError> handleServerError(
            Exception ex,
            HttpServletRequest request
    ) {
        return buildResponse(
                new RuntimeException("Unexpected server error"),
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


//@RestControllerAdvice
//public class GlobalExceptionHandler {
//
//    // 1. Parcel ไม่เจอ → 404
//    @ExceptionHandler(ParcelNotFoundException.class)
//    public ResponseEntity<ApiError> handleParcelNotFound(
//            ParcelNotFoundException ex,
//            HttpServletRequest request
//    ) {
//        ApiError error = new ApiError(
//                HttpStatus.NOT_FOUND.value(),                // 404
//                HttpStatus.NOT_FOUND.getReasonPhrase(),      // "Not Found"
//                ex.getMessage(),
//                request.getRequestURI()
//        );
//        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
//    }
//
//    // 2. Request/Argument ไม่ถูก → 400 (เช่น company ไม่เจอ, dormId null ฯลฯ)
//    @ExceptionHandler(IllegalArgumentException.class)
//    public ResponseEntity<ApiError> handleIllegalArgument(
//            IllegalArgumentException ex,
//            HttpServletRequest request
//    ) {
//        ApiError error = new ApiError(
//                HttpStatus.BAD_REQUEST.value(),              // 400
//                HttpStatus.BAD_REQUEST.getReasonPhrase(),    // "Bad Request"
//                ex.getMessage(),
//                request.getRequestURI()
//        );
//        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
//    }
//
//    // 3. อื่น ๆ ที่ไม่รู้จริง ๆ → 500
//    @ExceptionHandler(Exception.class)
//    public ResponseEntity<ApiError> handleException(
//            Exception ex,
//            HttpServletRequest request
//    ) {
//        ApiError error = new ApiError(
//                HttpStatus.INTERNAL_SERVER_ERROR.value(),        // 500
//                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
//                "Unexpected error occurred",                     // ไม่โชว์ detail จริง
//                request.getRequestURI()
//        );
//        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
//    }
//
//    // 4. Email ซ้ำ → 409 Conflict
//    @ExceptionHandler(EmailAlreadyExistsException.class)
//    public ResponseEntity<ApiError> handleEmailAlreadyExists(
//            EmailAlreadyExistsException ex,
//            HttpServletRequest request
//    ) {
//        ApiError error = new ApiError(
//                HttpStatus.CONFLICT.value(),                  // 409
//                HttpStatus.CONFLICT.getReasonPhrase(),        // "Conflict"
//                ex.getMessage(),
//                request.getRequestURI()
//        );
//        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
//    }
//    // 5. Unauthorized → 401
//    @ExceptionHandler(UnauthorizedException.class)
//    public ResponseEntity<ApiError> handleUnauthorized(
//            UnauthorizedException ex,
//            HttpServletRequest request
//    ) {
//        ApiError error = new ApiError(
//                HttpStatus.UNAUTHORIZED.value(),               // 401
//                HttpStatus.UNAUTHORIZED.getReasonPhrase(),     // "Unauthorized"
//                ex.getMessage(),
//                request.getRequestURI()
//        );
//        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
//    }
//}