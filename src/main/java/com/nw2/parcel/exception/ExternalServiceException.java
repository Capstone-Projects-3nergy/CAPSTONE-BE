package com.nw2.parcel.exception;

// 500 (สำหรับ external service error เช่น Firebase)
public class ExternalServiceException extends RuntimeException {
    public ExternalServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}