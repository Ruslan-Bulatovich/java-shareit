package ru.practicum.shareit.item.mapper;

import org.mapstruct.*;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;

@Mapper(componentModel = "spring")
public interface ItemMapper {
    @Mapping(source = "request.id", target = "requestId")
    ItemDtoResponse mapToItemDtoResponse(Item item);

    Item mapToItemFromItemDto(ItemDto itemDto);

    @Mapping(source = "booker.id", target = "bookerId")
    BookingShortDto mapToBookingShortDto(Booking booking);

    Comment mapToCommentFromCommentDto(CommentDto commentDto);

    @Mapping(source = "author.name", target = "authorName")
    CommentDtoResponse mapToCommentDtoResponseFromComment(Comment comment);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Item mapToItemFromItemDtoUpdate(ItemDtoUpdate itemDtoUpdate, @MappingTarget Item item);

}
