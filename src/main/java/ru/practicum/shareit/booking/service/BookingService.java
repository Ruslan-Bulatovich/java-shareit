package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingInputDto;
import ru.practicum.shareit.booking.model.AccessLevel;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.State;

import java.util.List;

public interface BookingService {

    BookingDto addBooking(long bookerId, BookingInputDto bookingInputDto);

    BookingDto approveOrRejectBooking(long ownerId, long bookingId, boolean approved, AccessLevel accessLevel);

    Booking getBookingById(long bookingId, long userId, AccessLevel accessLevel);

    BookingDto getBooking(long bookingId, long userId, AccessLevel accessLevel);

    List<BookingDto> getBookingsOfCurrentUser(State state, long bookerId);

    List<BookingDto> getBookingsOfOwner(State state, long ownerId);
}
