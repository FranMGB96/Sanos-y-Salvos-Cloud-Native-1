package com.sanosysalvos.userservice.exception;
public class BadRequestException extends RuntimeException {
    public BadRequestException(String msg) { super(msg); }
}
