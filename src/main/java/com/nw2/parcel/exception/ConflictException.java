package com.nw2.parcel.exception;

// 409
public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}