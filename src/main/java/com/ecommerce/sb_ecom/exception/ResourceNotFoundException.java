package com.ecommerce.sb_ecom.exception;

public class ResourceNotFoundException extends RuntimeException {
    String resourceName;
    String fieldName;
    String fieldValueStr;
    Long fieldValue;

    public ResourceNotFoundException() {
    }

    public ResourceNotFoundException(String resourceName, String fieldName, Long fieldValue) {
        super(String.format("%s not found with %s: %s", resourceName, fieldName, fieldValue));
        this.resourceName = resourceName;
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }

    public ResourceNotFoundException(String resourceName, String fieldName, String fieldValueStr) {
        super(String.format("%s not found with %s: %s", resourceName, fieldName, fieldValueStr));
        this.resourceName = resourceName;
        this.fieldName = fieldName;
        this.fieldValueStr = fieldValueStr;
    }
}
