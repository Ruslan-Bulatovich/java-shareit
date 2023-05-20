package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.shareit.booking.enums.State;
import ru.practicum.shareit.booking.enums.Status;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoResponse;
import ru.practicum.shareit.booking.dto.BookingListDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.error.handler.exception.StateException;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.model.User;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookings;
    private final UserRepository users;
    private final ItemRepository items;
    private final BookingMapper mapper;

    @Override
    @Transactional
    public BookingDtoResponse createBooking(Long bookerId, BookingDto bookingDto) {
        if (bookingDto.getEnd().isBefore(bookingDto.getStart())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Дата окончания бронирования не может быть раньше даты начала");
        }
        Item item = items.findById(bookingDto.getItemId()).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        String.format("Предмета с id=%s нет", bookingDto.getItemId())));
        if (!item.getOwner().getId().equals(bookerId)) {
            if (item.getAvailable()) {
                User user = users.findById(bookerId).orElseThrow(
                        () -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                String.format("Пользователя с id=%s нет", bookerId)));
                Booking booking = mapper.mapToBookingFromBookingDto(bookingDto);
                booking.setItem(item);
                booking.setBooker(user);
                return mapper.mapToBookingDtoResponse(bookings.save(booking));
            } else {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        String.format("Вещь с id=%s недоступна для бронирования", item.getId()));
            }
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Владелец не может забронировать свою вещь");
        }
    }

    @Override
    @Transactional
    public BookingDtoResponse approveBooking(Long ownerId, Long bookingId, String approved) {
        String approve = approved.toLowerCase();
        if (!(approve.equals("true") || approve.equals("false"))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Неккоректный параметр строки approved");
        }
        Booking booking = bookings.findById(bookingId).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        String.format("Бронирования с id=%s нет", bookingId)));
        if (!booking.getStatus().equals(Status.WAITING)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Невозможно изменить статус брони со статусом " + booking.getStatus());
        }
        if (booking.getItem().getOwner().getId().equals(ownerId)) {
            if (approve.equals("true")) {
                booking.setStatus(Status.APPROVED);
            } else {
                booking.setStatus(Status.REJECTED);
            }
            return mapper.mapToBookingDtoResponse(bookings.save(booking));
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    String.format("Пользователь с id=%s не является владельцем вещи с id=%s", ownerId, booking.getItem().getOwner().getId()));
        }
    }

    @Override
    @Transactional(readOnly = true)
    public BookingDtoResponse getBookingByIdForOwnerAndBooker(Long bookingId, Long userId) {
        Booking booking = bookings.findById(bookingId).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Бронирования с id=" + bookingId + " нет"));
        if (!(booking.getBooker().getId().equals(userId) || booking.getItem().getOwner().getId().equals(userId))) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    String.format("Пользователь с id=%s не является автором бронирования или владельцем вещи, к которой относится бронирование", userId));
        }
        return mapper.mapToBookingDtoResponse(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public BookingListDto getAllBookingsForUser(Pageable pageable, Long userId, String state) {
        if (!users.existsById(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    String.format("Пользователя с id=%s не существует", userId));
        } else {
            return getListBookings(pageable, state, userId, false);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public BookingListDto getAllBookingsForItemsUser(Pageable pageable, Long userId, String state) {
        if (!users.existsById(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("Пользователя с id=%s не существует", userId));
        }
        if (!items.existsItemByOwnerId(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    String.format("У пользователя с id=%s нет зарегестрированых вещей", userId));
        } else {
            return getListBookings(pageable, state, userId, true);
        }

    }

    private BookingListDto getListBookings(Pageable pageable, String state, Long userId, Boolean isOwner) {
        List<Long> itemsId;
        switch (State.fromValue(state.toUpperCase())) {
            case ALL:
                if (isOwner) {
                    itemsId = items.findAllItemIdByOwnerId(userId);
                    return BookingListDto.builder()
                            .bookings(bookings.findAllByItemIdInOrderByStartDesc(pageable, itemsId).stream()
                                    .map(mapper::mapToBookingDtoResponse).collect(Collectors.toList())).build();
                } else {
                    return BookingListDto.builder()
                            .bookings(bookings.findAllByBookerIdOrderByStartDesc(pageable, userId).stream()
                                    .map(mapper::mapToBookingDtoResponse).collect(Collectors.toList())).build();
                }
            case CURRENT:
                if (isOwner) {
                    itemsId = items.findAllItemIdByOwnerId(userId);
                    return BookingListDto.builder().bookings(
                            bookings.findAllByItemIdInAndStartIsBeforeAndEndIsAfterOrderByStartDesc(
                                            pageable, itemsId, LocalDateTime.now(), LocalDateTime.now()).stream()
                                    .map(mapper::mapToBookingDtoResponse).collect(Collectors.toList())).build();
                } else {
                    return BookingListDto.builder().bookings(
                            bookings.findAllByBookerIdAndStartIsBeforeAndEndIsAfterOrderByStartDesc(
                                            pageable, userId, LocalDateTime.now(), LocalDateTime.now()).stream()
                                    .map(mapper::mapToBookingDtoResponse).collect(Collectors.toList())).build();
                }
            case PAST:
                if (isOwner) {
                    itemsId = items.findAllItemIdByOwnerId(userId);
                    return BookingListDto.builder()
                            .bookings(bookings
                                    .findAllByItemIdInAndEndIsBeforeOrderByStartDesc(
                                            pageable, itemsId, LocalDateTime.now()
                                    ).stream().map(mapper::mapToBookingDtoResponse).collect(Collectors.toList()))
                            .build();
                } else {
                    return BookingListDto.builder()
                            .bookings(bookings
                                    .findAllByBookerIdAndEndIsBeforeOrderByStartDesc(
                                            pageable, userId, LocalDateTime.now()
                                    ).stream().map(mapper::mapToBookingDtoResponse).collect(Collectors.toList()))
                            .build();
                }
            case FUTURE:
                if (isOwner) {
                    itemsId = items.findAllItemIdByOwnerId(userId);
                    return BookingListDto.builder()
                            .bookings(bookings
                                    .findAllByItemIdInAndStartIsAfterOrderByStartDesc(pageable, itemsId, LocalDateTime.now())
                                    .stream().map(mapper::mapToBookingDtoResponse).collect(Collectors.toList()))
                            .build();
                } else {
                    return BookingListDto.builder()
                            .bookings(bookings
                                    .findAllByBookerIdAndStartIsAfterOrderByStartDesc(pageable, userId, LocalDateTime.now())
                                    .stream().map(mapper::mapToBookingDtoResponse).collect(Collectors.toList()))
                            .build();
                }
            case WAITING:
                if (isOwner) {
                    itemsId = items.findAllItemIdByOwnerId(userId);
                    return BookingListDto.builder()
                            .bookings(bookings
                                    .findAllByItemIdInAndStatusIsOrderByStartDesc(pageable, itemsId, Status.WAITING)
                                    .stream().map(mapper::mapToBookingDtoResponse).collect(Collectors.toList()))
                            .build();
                } else {
                    return BookingListDto.builder()
                            .bookings(bookings
                                    .findAllByBookerIdAndStatusIsOrderByStartDesc(pageable, userId, Status.WAITING)
                                    .stream().map(mapper::mapToBookingDtoResponse).collect(Collectors.toList()))
                            .build();
                }
            case REJECTED:
                if (isOwner) {
                    itemsId = items.findAllItemIdByOwnerId(userId);
                    return BookingListDto.builder()
                            .bookings(bookings
                                    .findAllByItemIdInAndStatusIsOrderByStartDesc(pageable, itemsId, Status.REJECTED)
                                    .stream().map(mapper::mapToBookingDtoResponse).collect(Collectors.toList()))
                            .build();
                } else {
                    return BookingListDto.builder()
                            .bookings(bookings
                                    .findAllByBookerIdAndStatusIsOrderByStartDesc(pageable, userId, Status.REJECTED)
                                    .stream().map(mapper::mapToBookingDtoResponse).collect(Collectors.toList()))
                            .build();
                }
            default:
                throw new StateException("Unknown state: " + state);
        }
    }
}
