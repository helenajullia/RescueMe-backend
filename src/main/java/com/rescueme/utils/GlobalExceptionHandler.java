package com.rescueme.utils;

import com.rescueme.exception.IncorrectCredentialsException;
import com.rescueme.exception.UsernameAlreadyExistException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IncorrectCredentialsException.class)
    public ResponseEntity<String> handleIncorrectCredentials(IncorrectCredentialsException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Incorrect credentials");
    }

    @ExceptionHandler(UsernameAlreadyExistException.class)
    public ResponseEntity<String> handleUsernameAlreadyExists(UsernameAlreadyExistException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGeneralException(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred");
    }
}

