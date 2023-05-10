package ru.practicum.shareit.logger;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;

@Slf4j
public class Logger {
    public static void logRequest(HttpMethod httpMethod, String url, String body) {
        log.info("Получен запрос {}{}. Тело запроса: {}", httpMethod, url, body);
    }

    public static void logSave(HttpMethod httpMethod, String url, String body) {
        log.info("По запросу {}{} получен слудющий результат: {}", httpMethod, url, body);
    }

    public static void logInfo(HttpMethod httpMethod, String url, String message) {
        log.info("Информация по запросу {}{}: {}", httpMethod, url, message);
    }
}