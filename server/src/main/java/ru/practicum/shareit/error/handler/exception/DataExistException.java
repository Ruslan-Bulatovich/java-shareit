package ru.practicum.shareit.error.handler.exception;

public class DataExistException extends RuntimeException {
    public DataExistException(String message) {
        super(message);
    }
}