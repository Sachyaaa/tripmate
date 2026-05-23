package com.tripmate.exception;

public class TripAccessDeniedException extends RuntimeException {
    public TripAccessDeniedException(String message) {
        super(message);
    }
}
