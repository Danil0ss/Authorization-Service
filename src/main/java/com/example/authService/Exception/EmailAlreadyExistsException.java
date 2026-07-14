package com.example.authService.Exception;

public class EmailAlreadyExistsException extends RuntimeException{
    public EmailAlreadyExistsException (String message){
        super(message);
    }
}
