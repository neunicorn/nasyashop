package com.nasya.ecommerce.common.erros;

public class BadRequestException extends RuntimeException {
    public BadRequestException(String message){
        super(message);
    }
}
