package se.comerit.avanza.exception;

/**
 * Exception thrown when a requested resource is not found. More specifically,
 * when a 404 error occurs.
 *
 * @param message the detail message explaining the reason for the exception
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}