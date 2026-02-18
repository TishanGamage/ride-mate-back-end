package com.ride.mate.exception;

public class ValidateRecordException extends RuntimeException{

    private final String field;

    public ValidateRecordException(String exception, String field) {
        super(exception);
        this.field = field;
    }

    public String getField() {
        return this.field;
    }
}
