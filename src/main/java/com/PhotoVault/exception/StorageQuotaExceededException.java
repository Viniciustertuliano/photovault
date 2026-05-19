package com.PhotoVault.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.PAYMENT_REQUIRED)
public class StorageQuotaExceededException extends BusinessException {
    public StorageQuotaExceededException(String message) {
        super(message);
    }
}
