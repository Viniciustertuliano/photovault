package com.PhotoVault.exception;

public class EmailAlreadyExistsException extends BusinessException{


    public EmailAlreadyExistsException(String message) {
        super(message);
    }

    public EmailAlreadyExistsException(String message, String email) {
        super(String.format("%s: %s", message, email));
    }
}
