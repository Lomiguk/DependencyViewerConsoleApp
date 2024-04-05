package ru.dsckibin.exception;

public class EmptyArgsException extends RuntimeException{
    public EmptyArgsException(String message) {
        super(message);
    }
}
