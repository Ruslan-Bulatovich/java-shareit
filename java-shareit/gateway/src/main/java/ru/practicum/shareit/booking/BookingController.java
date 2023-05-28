package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BookingController {

    private final BookingClient bookingClient;

    @PostMapping
    public ResponseEntity<Object> createBooking(@RequestHeader(Header.userIdHeader) @Min(1) Long bookerId,
                                                      @Valid @RequestBody BookingDto bookingDto) {
        return bookingClient.createBooking(bookerId, bookingDto);
    }

    @PatchMapping("{bookingId}")
    public ResponseEntity<Object> approveBooking(@RequestHeader(Header.userIdHeader) @Min(1) Long ownerId,
                                                       @RequestParam String approved,
                                                       @PathVariable @Min(1) Long bookingId) {
        return bookingClient.approveBooking(ownerId, approved, bookingId);
    }

    @GetMapping("{bookingId}")
    public ResponseEntity<Object> getBookingByIdForOwnerAndBooker(
            @PathVariable @Min(1) Long bookingId,
            @RequestHeader(Header.userIdHeader) @Min(1) Long userId) {
        return bookingClient.getBookingByIdForOwnerAndBooker(bookingId, userId);
    }

    @GetMapping
    public ResponseEntity<Object> getAllBookingsForUser(
            @RequestHeader(Header.userIdHeader) @Min(1) Long userId,
            @RequestParam(defaultValue = "ALL") String state,
            @RequestParam(value = "from", defaultValue = "0") @Min(0) Integer from,
            @RequestParam(value = "size", defaultValue = "10") @Min(1) @Max(20) Integer size) {
        return bookingClient.getAllBookingsForUser(userId, state, from, size);
    }

    @GetMapping("owner")
    public ResponseEntity<Object> getAllBookingsForItemsUser(
            @RequestHeader(Header.userIdHeader) @Min(1) Long userId,
            @RequestParam(defaultValue = "ALL") String state,
            @RequestParam(value = "from", defaultValue = "0") @Min(0) Integer from,
            @RequestParam(value = "size", defaultValue = "10") @Min(1) @Max(20) Integer size) {
        return bookingClient.getAllBookingsForItemsUser(userId, state, from, size);
    }
}
