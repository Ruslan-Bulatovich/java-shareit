package ru.practicum.shareit.booking.service;

import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoResponse;
import ru.practicum.shareit.booking.dto.BookingListDto;

public interface BookingService {
    /**
     * Бронирование
     *
     * @param bookerId   идентификатор
     * @param bookingDto Бронь
     * @return BookingDtoResponse
     */
    BookingDtoResponse createBooking(Long bookerId, BookingDto bookingDto);

    /**
     * Подтверждение запроса на бронирование
     *
     * @param ownerId   идентификатор Владельца
     * @param bookingId идентификатор Брони
     * @param approved  статус бронирования
     * @return BookingDtoResponse
     */
    BookingDtoResponse approveBooking(Long ownerId, Long bookingId, String approved);

    /**
     * Возвращает Бронирование по идентификатору
     *
     * @param bookingId идентификатор Брони
     * @param userId    идентификатор пользователя
     * @return BookingDtoResponse
     */
    BookingDtoResponse getBookingByIdForOwnerAndBooker(Long bookingId, Long userId);

    /**
     * Возвращает коллекцию Booking для текущего Пользователя
     *
     * @param pageable пагинация
     * @param userId   идентификатор Пользователя
     * @param state    состояние
     * @return коллекцию BookingListDto
     */
    BookingListDto getAllBookingsForUser(Pageable pageable, Long userId, String state);

    /**
     * Возвращает коллекцию Booking для Вещей текущего Пользователя
     *
     * @param pageable пагинация
     * @param userId   идентификатор Пользователя
     * @param state    состояние
     * @return коллекцию BookingListDto
     */
    BookingListDto getAllBookingsForItemsUser(Pageable pageable, Long userId, String state);
}
