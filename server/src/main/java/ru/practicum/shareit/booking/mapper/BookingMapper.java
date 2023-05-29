package ru.practicum.shareit.booking.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoResponse;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.dto.ItemShortDto;
import ru.practicum.shareit.item.model.Item;

@Mapper(componentModel = "spring")
public interface BookingMapper {

    Booking mapToBookingFromBookingDto(BookingDto bookingDto);

    BookingDtoResponse mapToBookingDtoResponse(Booking booking);

    ItemShortDto mapToItemShortDtoFromItem(Item item);

    @Mapping(target = "bookerId", source = "booker.id")
    BookingShortDto convertToDtoShort(Booking booking);
}
