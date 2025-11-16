package org.example.exception;

public class UplinkNotFound extends RuntimeException {
    public UplinkNotFound(String message) {
        super(message);
    }
}
