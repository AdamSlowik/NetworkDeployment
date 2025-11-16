package org.example.exception;

public class NodeNotFound extends RuntimeException {
    public NodeNotFound(String message) {
        super(message);
    }
}
