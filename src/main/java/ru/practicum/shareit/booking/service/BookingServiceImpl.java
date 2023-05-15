package ru.practicum.shareit.booking.service;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingInputDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.AccessLevel;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.State;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.AccessException;
import ru.practicum.shareit.exception.InvalidDataException;
import ru.practicum.shareit.exception.ObjectNotAvailableException;
import ru.practicum.shareit.exception.ObjectNotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class BookingServiceImpl implements BookingService {
    private final ItemRepository itemRepository;
    private final UserService userService;
    private final UserMapper userMapper;
    private final BookingMapper bookingMapper;
    private final BookingRepository bookingRepository;

    @Transactional
    @Override
    public BookingDto addBooking(long bookerId, BookingInputDto bookingInputDto) {
        Booking booking = bookingMapper.convertFromInputDto(bookingInputDto);
        User booker = userMapper.convertFromDto(userService.getUser(bookerId));
        Item item = itemRepository.findById(booking.getItem().getId()).orElseThrow(() ->
                new ObjectNotFoundException(String.format("Вещь с id %s не найдена", booking.getItem().getId())));
        validateAddBooking(bookerId, booking, item);
        booking.setBooker(booker);
        booking.setStatus(Status.WAITING);
        booking.setItem(item);
        Booking bookingSaved = bookingRepository.save(booking);
        return bookingMapper.convertToDto(bookingSaved);
    }

    @Transactional
    @Override
    public BookingDto approveOrRejectBooking(long ownerId, long bookingId, boolean approved, AccessLevel accessLevel) {
        User owner = userMapper.convertFromDto(userService.getUser(ownerId));
        Booking booking = bookingMapper.convertFromInputDto(getBooking(bookingId, owner.getId(), accessLevel));
        if (booking.getStatus().equals(Status.APPROVED)) {
            throw new InvalidDataException(String.format("У бронирования с id %d уже стоит статус %s",
                    bookingId, Status.APPROVED.name()));
        }
        if (approved) {
            booking.setStatus(Status.APPROVED);
        } else {
            booking.setStatus(Status.REJECTED);
        }
        Booking bookingSaved = bookingRepository.save(booking);
        return bookingMapper.convertToDto(bookingSaved);
    }

    @Transactional(readOnly = true)
    @Override
    public BookingDto getBooking(long bookingId, long userId, AccessLevel accessLevel) {
        User user = userMapper.convertFromDto(userService.getUser(userId));
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(
                () -> new ObjectNotFoundException(String.format("Бронирование с id %d не найдено", bookingId)));
        if (isUnableToAccess(user.getId(), booking, accessLevel)) {
            throw new AccessException(String.format("У пользователя с id %d нет прав на просмотр бронирования с id %d,",
                    userId, bookingId));
        }
        return bookingMapper.convertToDto(booking);
    }

    @Transactional(readOnly = true)
    @Override
    public List<BookingDto> getBookingsOfCurrentUser(State state, long bookerId) {
        User booker = userMapper.convertFromDto(userService.getUser(bookerId));
        Sort sort = Sort.by(Sort.Direction.DESC, "start");
        List<Booking> bookings;
        switch (state) {
            case WAITING:
                bookings = bookingRepository.findAllByBookerIdAndStatus(booker.getId(),
                        Status.WAITING, sort);
                break;
            case REJECTED:
                bookings = bookingRepository.findAllByBookerIdAndStatus(booker.getId(),
                        Status.REJECTED, sort);
                break;
            case PAST:
                bookings = bookingRepository.findAllByBookerIdAndEndBefore(booker.getId(),
                        LocalDateTime.now(), sort);
                break;
            case FUTURE:
                bookings = bookingRepository.findAllByBookerIdAndStartAfter(booker.getId(),
                        LocalDateTime.now(), sort);
                break;
            case CURRENT:
                bookings = bookingRepository.findAllByBookerIdAndStartBeforeAndEndAfter(booker.getId(),
                        LocalDateTime.now(), sort);
                break;
            default:
                bookings = bookingRepository.findAllByBookerId(booker.getId(), sort);
        }
        return bookings
                .stream()
                .map(bookingMapper::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Override
    public List<BookingDto> getBookingsOfOwner(State state, long ownerId) {
        User owner = userMapper.convertFromDto(userService.getUser(ownerId));
        Sort sort = Sort.by(Sort.Direction.DESC, "start");
        List<Booking> bookings;
        switch (state) {
            case WAITING:
                bookings = bookingRepository.findAllByOwnerIdAndStatus(owner.getId(),
                        Status.WAITING, sort);
                break;
            case REJECTED:
                bookings = bookingRepository.findAllByOwnerIdAndStatus(owner.getId(),
                        Status.REJECTED, sort);
                break;
            case PAST:
                bookings = bookingRepository.findAllByOwnerIdAndEndBefore(owner.getId(),
                        LocalDateTime.now(), sort);
                break;
            case FUTURE:
                bookings = bookingRepository.findAllByOwnerIdAndStartAfter(owner.getId(),
                        LocalDateTime.now(), sort);
                break;
            case CURRENT:
                bookings = bookingRepository.findAllByOwnerIdAndStartBeforeAndEndAfter(owner.getId(),
                        LocalDateTime.now(), sort);
                break;
            default:
                bookings = bookingRepository.findAllByOwnerId(owner.getId(), sort);
        }
        return bookings.stream()
                .map(bookingMapper::convertToDto)
                .collect(Collectors.toList());
    }

    private boolean isNotValidDate(LocalDateTime startBooking, LocalDateTime endBooking) {
        return startBooking.isBefore(LocalDateTime.now()) || endBooking.isBefore(LocalDateTime.now())
                || endBooking.isBefore(startBooking) || endBooking.isEqual(startBooking);
    }

    private boolean isUnableToAccess(long userId, Booking booking, AccessLevel accessLevel) {
        boolean isUnable = true;
        switch (accessLevel) {
            case OWNER:
                isUnable = booking.getItem().getUserId() != userId;
                break;
            case BOOKER:
                isUnable = booking.getBooker().getId() != userId;
                break;
            case OWNER_AND_BOOKER:
                isUnable = !(booking.getItem().getUserId() == userId || booking.getBooker().getId() == userId);
                break;
        }
        return isUnable;
    }

    private void validateAddBooking(long bookerId, Booking booking, Item item) {
        if (bookerId == item.getUserId()) {
            throw new AccessException("Владелец вещи не может бронировать свои вещи.");
        } else if (!item.getAvailable()) {
            throw new ObjectNotAvailableException(String.format("Вещь с id %d не доступна для бронирования.",
                    item.getId()));
        } else if (isNotValidDate(booking.getStart(), booking.getEnd())) {
            throw new InvalidDataException("Даты бронирования выбраны некорректно.");
        }
    }
}
