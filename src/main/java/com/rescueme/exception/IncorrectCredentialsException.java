package com.rescueme.exception;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class IncorrectCredentialsException extends RuntimeException {

    public IncorrectCredentialsException(String message) {
        super(message);
    }
}
