package ru.practicum.shareit.booking.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingInputDto;
import ru.practicum.shareit.booking.model.AccessLevel;
import ru.practicum.shareit.booking.model.State;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.logger.Logger;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping(path = "/bookings")
@AllArgsConstructor
public class BookingController {
    private final BookingService bookingService;
    private final String host = "localhost";
    private final String port = "8080";
    private final String protocol = "http";
    private final String userIdHeader = "X-Sharer-User-Id";

    @PostMapping    // Добавление нового запроса на бронирование.
    public ResponseEntity<BookingDto> addBooking(@RequestHeader(userIdHeader) long userId,
                                                 @Valid @RequestBody BookingInputDto bookingInputDto) {
        UriComponents uriComponents = UriComponentsBuilder.newInstance()
                .scheme(protocol)
                .host(host)
                .port(port)
                .path("/bookings")
                .build();
        Logger.logRequest(HttpMethod.POST, uriComponents.toUriString(), bookingInputDto.toString());
        return ResponseEntity.status(201).body(bookingService.addBooking(userId, bookingInputDto));
    }

    @PatchMapping("/{bookingId}")   // Подтверждение или отклонение запроса на бронирование.
    public ResponseEntity<BookingDto> approveOrRejectBooking(@PathVariable long bookingId, @RequestParam boolean approved,
                                                             @RequestHeader(userIdHeader) long userId) {
        UriComponents uriComponents = UriComponentsBuilder.newInstance()
                .scheme(protocol)
                .host(host)
                .port(port)
                .path("/bookings/")
                .query("approved={approved}")
                .build();
        Logger.logRequest(HttpMethod.PATCH, uriComponents.toUriString(), "no body");
        return ResponseEntity.ok().body(bookingService.approveOrRejectBooking(userId, bookingId, approved, AccessLevel.OWNER));
    }

    @GetMapping("/{bookingId}")   // Получение данных о конкретном бронировании (включая его статус)
    public ResponseEntity<BookingDto> getBookingById(@PathVariable long bookingId, @RequestHeader(userIdHeader) long userId) {
        UriComponents uriComponents = UriComponentsBuilder.newInstance()
                .scheme(protocol)
                .host(host)
                .port(port)
                .path("/bookings/{bookingId}")
                .build();
        Logger.logRequest(HttpMethod.GET, uriComponents.toUriString(), "no body");
        return ResponseEntity.ok().body(bookingService.getBooking(bookingId, userId, AccessLevel.OWNER_AND_BOOKER));
    }

    @GetMapping   // Получение списка всех бронирований текущего пользователя (можно делать выборку по статусу).
    public ResponseEntity<List<BookingDto>> getBookingsOfCurrentUser(@RequestParam(defaultValue = "ALL") String state,
                                                                     @RequestHeader(userIdHeader) long userId) {
        UriComponents uriComponents = UriComponentsBuilder.newInstance()
                .scheme(protocol)
                .host(host)
                .port(port)
                .path("/bookings/")
                .query("state={state}")
                .build();
        Logger.logRequest(HttpMethod.GET, uriComponents.toUriString(), "no body");
        return ResponseEntity.ok().body(bookingService.getBookingsOfCurrentUser(State.convert(state), userId));
    }

    // Получение списка бронирований для всех вещей текущего пользователя-владельца (можно делать выборку по статусу)
    @GetMapping("/owner")
    public ResponseEntity<List<BookingDto>> getBookingsOfOwner(@RequestParam(defaultValue = "ALL") String state,
                                                               @RequestHeader(userIdHeader) long userId) {
        UriComponents uriComponents = UriComponentsBuilder.newInstance()
                .scheme(protocol)
                .host(host)
                .port(port)
                .path("/bookings/owner")
                .query("state={state}")
                .build();
        Logger.logRequest(HttpMethod.GET, uriComponents.toUriString(), "no body");
        return ResponseEntity.ok().body(bookingService.getBookingsOfOwner(State.convert(state), userId));
    }
}
