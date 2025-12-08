package com.kompu.api.entity.appconfig.exception;

public class AppConfigNotFoundException extends RuntimeException {

    public AppConfigNotFoundException() {
        super("Application configuration not found in the system");
    }

    public AppConfigNotFoundException(String message) {
        super(message);
    }

}
