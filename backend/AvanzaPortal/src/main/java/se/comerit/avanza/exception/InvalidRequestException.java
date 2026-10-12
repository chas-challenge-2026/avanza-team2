package se.comerit.avanza.exception;

/**
 * Exception thrown when a request is invalid. More specifically, when 400 error
 * occurs.
 * 
 * @param message the detail message explaining the reason for the exception
 */
public class InvalidRequestException extends RuntimeException {
    public InvalidRequestException(String message) {
        super(message);
    }
}