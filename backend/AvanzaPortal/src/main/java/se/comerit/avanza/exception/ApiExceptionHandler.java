package se.comerit.avanza.exception;

import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import jakarta.validation.ConstraintViolationException;

/**
 * This is single global exception handler for the possible exceptions that API
 * enpoints could encounter.
 * What it does is that it catches various exceptions thrown by the API
 * endpoints and returns appropriate HTTP responses with detailed error
 * information.
 * <p/>
 * Messages are sent through classes called exception.java
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler({
            InvalidRequestException.class,
            UnsupportedCurrencyPairException.class
    })
    public ResponseEntity<ProblemDetail> handleBadRequest(RuntimeException exception) {
        return problem(HttpStatus.BAD_REQUEST, "Bad Request", exception.getMessage());
    }

    @ExceptionHandler(FxDataUnavailableException.class)
    public ResponseEntity<ProblemDetail> handleFxDataUnavailable(FxDataUnavailableException exception) {
        return problem(HttpStatus.SERVICE_UNAVAILABLE, "Service Unavailable", exception.getMessage());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleNotFound(ResourceNotFoundException exception) {
        return problem(HttpStatus.NOT_FOUND, "Not Found", exception.getMessage());
    }

    @ExceptionHandler({
            NoHandlerFoundException.class,
            NoResourceFoundException.class
    })
    public ResponseEntity<ProblemDetail> handleMissingEndpoint() {
        return problem(HttpStatus.NOT_FOUND, "Not Found", "The requested endpoint does not exist.");
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ProblemDetail> handleBadCredentials() {
        return problem(HttpStatus.UNAUTHORIZED, "Unauthorized", "Invalid username or password.");
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ProblemDetail> handleAccessDenied() {
        return problem(HttpStatus.FORBIDDEN, "Forbidden", "You are not allowed to perform this action.");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleValidation(MethodArgumentNotValidException exception) {
        Map<String, String> errors = exception.getBindingResult().getAllErrors().stream()
                .collect(Collectors.toMap(
                        error -> error instanceof FieldError fieldError
                                ? fieldError.getField()
                                : error.getObjectName(),
                        error -> error.getDefaultMessage() == null ? "Invalid value." : error.getDefaultMessage(),
                        (first, second) -> first + "; " + second,
                        TreeMap::new));

        ProblemDetail body = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "One or more request fields are invalid.");
        body.setTitle("Validation Failed");
        body.setProperty("errors", errors);
        return response(HttpStatus.BAD_REQUEST, body);
    }

    @ExceptionHandler({
            HttpMessageNotReadableException.class,
            MissingServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class
    })
    public ResponseEntity<ProblemDetail> handleMalformedRequest() {
        return problem(HttpStatus.BAD_REQUEST, "Bad Request", "The request is malformed or contains invalid values.");
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ProblemDetail> handleConstraintViolation() {
        return problem(HttpStatus.BAD_REQUEST, "Validation Failed",
                "One or more request parameters are invalid.");
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ProblemDetail> handleMethodNotAllowed() {
        return problem(HttpStatus.METHOD_NOT_ALLOWED, "Method Not Allowed",
                "The HTTP method is not supported for this endpoint.");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleUnexpectedException(Exception exception) {
        logger.error("Unexpected exception while handling API request", exception);
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error",
                "An unexpected error occurred.");
    }

    private ResponseEntity<ProblemDetail> problem(HttpStatus status, String title, String detail) {
        ProblemDetail body = ProblemDetail.forStatusAndDetail(
                status,
                detail == null || detail.isBlank() ? title : detail);
        body.setTitle(title);
        return response(status, body);
    }

    private ResponseEntity<ProblemDetail> response(HttpStatus status, ProblemDetail body) {
        return ResponseEntity.status(status)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(body);
    }
}