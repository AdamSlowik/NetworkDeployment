package org.example.controller;

import org.example.exception.MalformedMacAddress;
import org.example.exception.NodeNotFound;
import org.example.exception.UplinkNotFound;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

@RestControllerAdvice
public class ControllerAdvice {

    @ExceptionHandler(MalformedMacAddress.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    void handleMalformedMacAddress(MalformedMacAddress ex, WebRequest request) {
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    void handleConstraintViolation(DataIntegrityViolationException ex, WebRequest request) {
    }

    @ExceptionHandler(UplinkNotFound.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    void handleUplinkNotFound(UplinkNotFound ex, WebRequest request) {
    }

    @ExceptionHandler(NodeNotFound.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    void handleNodeNotFound(NodeNotFound ex, WebRequest request) {
    }
}
