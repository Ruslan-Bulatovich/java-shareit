package ru.practicum.shareit.booking.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoResponse;
import ru.practicum.shareit.booking.dto.BookingListDto;
import ru.practicum.shareit.booking.service.BookingService;


@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BookingController {

    private final BookingService bookingService;
    private static final String userIdHeader = "X-Sharer-User-Id";

    @PostMapping
    public ResponseEntity<BookingDtoResponse> createBooking(@RequestHeader(userIdHeader) Long bookerId,
                                                            @RequestBody BookingDto bookingDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bookingService.createBooking(bookerId, bookingDto));
    }

    @PatchMapping("{bookingId}")
    public ResponseEntity<BookingDtoResponse> approveBooking(@RequestHeader(userIdHeader) Long ownerId,
                                                             @RequestParam String approved,
                                                             @PathVariable Long bookingId) {
        return ResponseEntity.status(HttpStatus.OK).body(bookingService.approveBooking(ownerId, bookingId, approved));
    }

    @GetMapping("{bookingId}")
    public ResponseEntity<BookingDtoResponse> getBookingByIdForOwnerAndBooker(
            @PathVariable Long bookingId,
            @RequestHeader(userIdHeader) Long userId) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(bookingService.getBookingByIdForOwnerAndBooker(bookingId, userId));
    }

    @GetMapping
    public ResponseEntity<BookingListDto> getAllBookingsForUser(
            @RequestHeader(userIdHeader) Long userId,
            @RequestParam(defaultValue = "ALL") String state,
            @RequestParam(value = "from", defaultValue = "0") Integer from,
            @RequestParam(value = "size", defaultValue = "10") Integer size) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(bookingService.getAllBookingsForUser(PageRequest.of(from / size, size), userId, state));
    }

    @GetMapping("owner")
    public ResponseEntity<BookingListDto> getAllBookingsForItemsUser(
            @RequestHeader(userIdHeader) Long userId,
            @RequestParam(defaultValue = "ALL") String state,
            @RequestParam(value = "from", defaultValue = "0") Integer from,
            @RequestParam(value = "size", defaultValue = "10") Integer size) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(bookingService.getAllBookingsForItemsUser(PageRequest.of(from / size, size), userId, state));
    }
}
