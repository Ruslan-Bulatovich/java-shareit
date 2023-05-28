package ru.practicum.shareit.handler;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.shareit.handler.exception.StateException;
import ru.practicum.shareit.handler.responce.StateErrorResponse;

import javax.validation.ConstraintViolationException;
import java.util.Objects;

@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler(ResponseStatusException.class)
    private ResponseEntity<String> handleException(ResponseStatusException exception) {
        String message = Objects.requireNonNull(exception.getMessage()).replace(HttpStatus.BAD_REQUEST.toString(), "");
        return ResponseEntity
                .status(exception.getStatus())
                .body(message);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    private ResponseEntity<String> handleException(MethodArgumentNotValidException exception) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(HttpStatus.BAD_REQUEST + " " + Objects.requireNonNull(exception.getFieldError()).getDefaultMessage());
    }


    @ExceptionHandler(ConstraintViolationException.class)
    private ResponseEntity<String> handleException(ConstraintViolationException exception) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(HttpStatus.BAD_REQUEST + " " + exception.getMessage());
    }

    @ExceptionHandler(StateException.class)
    private ResponseEntity<StateErrorResponse> handleException(StateException exception) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new StateErrorResponse(exception.getMessage()));
    }
}
