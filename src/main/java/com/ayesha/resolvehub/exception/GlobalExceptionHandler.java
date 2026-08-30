package com.ayesha.resolvehub.exception;

import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(TicketNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, Object> handleTicketNotFound(
        TicketNotFoundException exception
    ) {
        return Map.of("status", 404, "message", exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleValidationErrors(
        MethodArgumentNotValidException exception
    ) {
        Map<String, String> errors = new HashMap<>();

        exception
            .getBindingResult()
            .getFieldErrors()
            .forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
            );

        return Map.of(
            "status",
            400,
            "message",
            "Validation failed",
            "errors",
            errors
        );
    }

    @ExceptionHandler(ProjectNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, Object> handleProjectNotFound(
        ProjectNotFoundException exception
    ) {
        return Map.of("status", 404, "message", exception.getMessage());
    }

    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, Object> handleUserNotFound(
        UserNotFoundException exception
    ) {
        return Map.of("status", 404, "message", exception.getMessage());
    }

    @ExceptionHandler(InvalidTicketStatusTransitionException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleInvalidTicketStatusTransition(
        InvalidTicketStatusTransitionException exception
    ) {
        return Map.of("status", 400, "message", exception.getMessage());
    }
}
