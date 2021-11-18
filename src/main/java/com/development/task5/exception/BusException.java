package com.development.task5.exception;

public class BusException extends Exception{
    public BusException() {
        super();
    }

    public BusException(String message) {
        super(message);
    }

    public BusException(String message, Throwable cause) {
        super(message, cause);
    }

    public BusException(Throwable cause) {
        super(cause);
    }
}
