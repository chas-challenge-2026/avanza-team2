package se.comerit.avanza.exception;

/**
 * Exception thrown when an unsupported currency pair is encountered. More
 * specifically,
 * when a 405 error occurs.
 *
 * @param message the detail message explaining the reason for the exception
 */
public class UnsupportedCurrencyPairException extends RuntimeException {
    public UnsupportedCurrencyPairException(String message) {
        super(message);
    }
}