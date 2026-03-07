package com.nw2.parcel.services;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class LineStateStore {

    private final Map<String, String> store = new ConcurrentHashMap<>();

    public void put(String state, String firebaseUid) {
        store.put(state, firebaseUid);
    }

    public String get(String state) {
        return store.remove(state);
    }
}