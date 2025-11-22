package com.nw2.parcel.exception;

public class ParcelNotFoundException extends RuntimeException {
    public ParcelNotFoundException(Integer id) {
        super("Parcel not found with id: " + id);
    }
}
