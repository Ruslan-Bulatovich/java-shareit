package ru.practicum.shareit.error.handler;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.shareit.error.handler.exception.StateException;
import ru.practicum.shareit.error.handler.responce.StateErrorResponse;

import javax.validation.ConstraintViolationException;


@RestControllerAdvice("ru.practicum.shareit")
public class ErrorHandler {

    @ExceptionHandler(ResponseStatusException.class)
    private ResponseEntity<String> handleException(ResponseStatusException exception) {
        return ResponseEntity
                .status(exception.getStatus())
                .body(exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    private ResponseEntity<String> handleException(MethodArgumentNotValidException exception) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(HttpStatus.BAD_REQUEST + " " + exception.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    private ResponseEntity<String> handleException() {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(HttpStatus.INTERNAL_SERVER_ERROR + " Нарушение уникального индекса или первичного ключа");
    }

    @ExceptionHandler(StateException.class)
    private ResponseEntity<StateErrorResponse> handleException(StateException exception) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new StateErrorResponse(exception.getMessage()));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    private ResponseEntity<String> handleException(ConstraintViolationException exception) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(HttpStatus.BAD_REQUEST + " " + exception.getMessage());
    }
}
