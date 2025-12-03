package com.PhotoVault.exception;

public class ResourceNotFoundException extends BusinessException{

    public ResourceNotFoundException(String resourceName){
        super(String.format("%s not found", resourceName ));
    }

    public ResourceNotFoundException(String resourceName, Long id ){
        super(String.format("%s not found with id: %d", resourceName, id));
    }

    public ResourceNotFoundException(String resourceName, String fieldName, String fieldValue ){
        super(String.format("%s not found with %s: %s", resourceName, fieldName, fieldValue));
    }
}
