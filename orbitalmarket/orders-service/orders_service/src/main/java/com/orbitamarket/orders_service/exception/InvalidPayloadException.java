package com.orbitamarket.orders_service.exception;

import lombok.Getter;

@Getter
public class InvalidPayloadException extends RuntimeException {
    private final String errorCode;
    public InvalidPayloadException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}