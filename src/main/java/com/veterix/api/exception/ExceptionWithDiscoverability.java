package com.veterix.api.exception;

import org.springframework.hateoas.EntityModel;

public abstract class ExceptionWithDiscoverability extends RuntimeException{
    public abstract EntityModel<Exception>  toEntityModel();

    public ExceptionWithDiscoverability() {
    }

    public ExceptionWithDiscoverability(String message) {
        super(message);
    }

    public ExceptionWithDiscoverability(String message, Throwable cause) {
        super(message, cause);
    }

    public ExceptionWithDiscoverability(Throwable cause) {
        super(cause);
    }

    public ExceptionWithDiscoverability(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
