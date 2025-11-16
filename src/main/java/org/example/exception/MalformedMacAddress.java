package org.example.exception;

public class MalformedMacAddress extends RuntimeException {
    public MalformedMacAddress(String message) {
        super(message);
    }
}
