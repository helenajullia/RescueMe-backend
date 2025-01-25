package com.rescueme.exception;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class EmailAlreadyExistException extends RuntimeException {

    public EmailAlreadyExistException(String message) {
        super(message);
    }
}

