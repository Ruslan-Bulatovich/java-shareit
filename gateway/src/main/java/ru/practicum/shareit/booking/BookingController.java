package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.common.Header;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

@RestController
@RequestMapping("/bookings")
@Validated
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BookingController {

    private final BookingClient bookingClient;

    @PostMapping
    public ResponseEntity<Object> createBooking(@RequestHeader(Header.userIdHeader) @Min(1) Long bookerId,
                                                @Valid @RequestBody BookingDto bookingDto) {
        log.info("Create booking {} by userId={}", bookingDto, bookerId);
        return bookingClient.createBooking(bookerId, bookingDto);
    }

    @PatchMapping("{bookingId}")
    public ResponseEntity<Object> approveBooking(@RequestHeader(Header.userIdHeader) @Min(1) Long ownerId,
                                                 @RequestParam String approved,
                                                 @PathVariable @Min(1) Long bookingId) {
        log.info("Update bookingId={}", bookingId);
        return bookingClient.approveBooking(ownerId, approved, bookingId);
    }

    @GetMapping("{bookingId}")
    public ResponseEntity<Object> getBookingByIdForOwnerAndBooker(
            @PathVariable @Min(1) Long bookingId,
            @RequestHeader(Header.userIdHeader) @Min(1) Long userId) {
        log.info("Get booking by userId={}", bookingId);
        return bookingClient.getBookingByIdForOwnerAndBooker(bookingId, userId);
    }

    @GetMapping
    public ResponseEntity<Object> getAllBookingsForUser(
            @RequestHeader(Header.userIdHeader) @Min(1) Long userId,
            @RequestParam(defaultValue = "ALL") String state,
            @RequestParam(value = "from", defaultValue = "0") @Min(0) Integer from,
            @RequestParam(value = "size", defaultValue = "10") @Min(1) @Max(20) Integer size) {
        log.info("Get all booking by userId={}", userId);
        return bookingClient.getAllBookingsForUser(userId, state, from, size);
    }

    @GetMapping("owner")
    public ResponseEntity<Object> getAllBookingsForItemsUser(
            @RequestHeader(Header.userIdHeader) @Min(1) Long userId,
            @RequestParam(defaultValue = "ALL") String state,
            @RequestParam(value = "from", defaultValue = "0") @Min(0) Integer from,
            @RequestParam(value = "size", defaultValue = "10") @Min(1) @Max(20) Integer size) {
        log.info("Get all booking by items userId={}", userId);
        return bookingClient.getAllBookingsForItemsUser(userId, state, from, size);
    }
}
