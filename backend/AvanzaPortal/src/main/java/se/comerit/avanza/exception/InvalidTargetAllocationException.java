package se.comerit.avanza.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidTargetAllocationException extends RuntimeException {

    public InvalidTargetAllocationException(String message) {
        super(message);
    }
    
}
