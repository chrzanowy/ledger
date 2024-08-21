package com.chrzanowy.events.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class InvalidEventException extends ResponseStatusException {

    public InvalidEventException() {
        super(HttpStatus.BAD_REQUEST, "Invalid event");
    }
}
