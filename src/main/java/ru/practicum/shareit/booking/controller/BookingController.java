package ru.practicum.shareit.booking.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingInputDto;
import ru.practicum.shareit.booking.model.AccessLevel;
import ru.practicum.shareit.booking.model.State;
import ru.practicum.shareit.booking.service.BookingService;

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

    @PostMapping()
    public ResponseEntity<BookingDto> addBooking(@RequestHeader(userIdHeader) long userId,
                                                 @Valid @RequestBody BookingInputDto bookingInputDto) {
        return ResponseEntity.status(201).body(bookingService.addBooking(userId, bookingInputDto));
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<BookingDto> approveOrRejectBooking(@PathVariable long bookingId, @RequestParam boolean approved,
                                                             @RequestHeader(userIdHeader) long userId) {
        return ResponseEntity.ok().body(bookingService.approveOrRejectBooking(userId, bookingId, approved, AccessLevel.OWNER));
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<BookingDto> getBookingById(@PathVariable long bookingId, @RequestHeader(userIdHeader) long userId) {
        return ResponseEntity.ok().body(bookingService.getBooking(bookingId, userId, AccessLevel.OWNER_AND_BOOKER));
    }

    @GetMapping()
    public ResponseEntity<List<BookingDto>> getBookingsOfCurrentUser(@RequestParam(defaultValue = "ALL") String state,
                                                                     @RequestHeader(userIdHeader) long userId) {
        return ResponseEntity.ok().body(bookingService.getBookingsOfCurrentUser(State.convert(state), userId));
    }

    @GetMapping("/owner")
    public ResponseEntity<List<BookingDto>> getBookingsOfOwner(@RequestParam(defaultValue = "ALL") String state,
                                                               @RequestHeader(userIdHeader) long userId) {
        return ResponseEntity.ok().body(bookingService.getBookingsOfOwner(State.convert(state), userId));
    }
}
