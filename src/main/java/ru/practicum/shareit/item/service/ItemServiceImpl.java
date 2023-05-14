package ru.practicum.shareit.item.service;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import ru.practicum.shareit.booking.dto.BookingDtoShort;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.InvalidDataException;
import ru.practicum.shareit.exception.ObjectNotAvailableException;
import ru.practicum.shareit.exception.ObjectNotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserService userService;
    private final BookingRepository bookingRepository;
    private final ItemMapper itemMapper;
    private final BookingMapper bookingMapper;
    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;


    @Transactional
    @Override
    public ItemDto addItem(long userId, ItemDto itemDto) {
        if (userId == 0L) {
            throw new InvalidDataException("Owner ID не может быть равен 0");
        }

        if (itemDto.getName() == null || itemDto.getName().isBlank()) {
            throw new InvalidDataException("Название не может быть пустой");
        } else if (itemDto.getDescription() == null || itemDto.getDescription().isBlank()) {
            throw new InvalidDataException("Описание не может быть пустой");
        } else if (itemDto.getAvailable() == null) {
            throw new InvalidDataException("Статус не может быть пустой");
        } else {
            User user = userService.getUserById(userId);
            Item item = itemMapper.convertFromDto(itemDto);
            item.setUserId(user.getId());
            Item itemSaved = itemRepository.save(item);
            return itemMapper.convertToDto(itemSaved);
        }
    }

    @Transactional
    @Override
    public ItemDto updateItem(long userId, long itemId, ItemDto itemDto) {
        Item item = itemMapper.convertFromDto(itemDto);
        User user = userService.getUserById(userId);
        Item targetItem = itemRepository.findById(itemId).orElseThrow(() ->
                new ObjectNotFoundException(String.format("Вещь с id %s не найдена", itemId)));
        if (targetItem.getUserId() != user.getId()) {
            throw new ObjectNotFoundException(String.format("У пользователя с id %s не найдена вещь с id %s",
                    userId, itemId));
        } else {
            if (item.getAvailable() != null) {
                targetItem.setAvailable(item.getAvailable());
            }
            if (StringUtils.hasLength(item.getName())) {
                targetItem.setName(item.getName());
            }
            if (StringUtils.hasLength(item.getDescription())) {
                targetItem.setDescription(item.getDescription());
            }
            Item itemSaved = itemRepository.save(targetItem);
            return itemMapper.convertToDto(itemSaved);
        }
    }

    @Transactional(readOnly = true)
    @Override
    public ItemDto getItemById(long itemId, long userId) {
        userService.getUserById(userId);
        Item item = itemRepository.findById(itemId).orElseThrow(() ->
                new ObjectNotFoundException(String.format("Вещь с id %s не найдена", itemId)));
        ItemDto itemDto = itemMapper.convertToDto(item);
        List<Booking> bookings = bookingRepository.findByItemId(itemId,
                Sort.by(Sort.Direction.DESC, "start"));
        List<BookingDtoShort> bookingDtoShorts = bookings.stream()
                .map(bookingMapper::convertToDtoShort)
                .collect(Collectors.toList());
        if (item.getUserId() == userId) {   // Бронирования показываем только владельцу вещи
            setBookings(itemDto, bookingDtoShorts);
        }
        List<Comment> comments = commentRepository.findAllByItemId(itemId,
                Sort.by(Sort.Direction.DESC, "created"));
        List<CommentDto> commentsDto = comments.stream()
                .map(commentMapper::convertToDto)
                .collect(Collectors.toList());
        itemDto.setComments(commentsDto);
        return itemDto;
    }

    @Transactional(readOnly = true)
    @Override
    public List<ItemDto> getAllItems(long userId) {
        User user = userService.getUserById(userId);
        List<Item> items = itemRepository.findAllByUserIdOrderById(user.getId());
        List<ItemDto> itemsDto = items.stream()
                .map(itemMapper::convertToDto)
                .collect(Collectors.toList());
        List<Booking> bookings = bookingRepository.findAllByOwnerId(userId,
                Sort.by(Sort.Direction.DESC, "start"));
        List<BookingDtoShort> bookingDtoShorts = bookings.stream()
                .map(bookingMapper::convertToDtoShort)
                .collect(Collectors.toList());
        List<Comment> comments = commentRepository.findAllByItemIdIn(
                items.stream()
                        .map(Item::getId)
                        .collect(Collectors.toList()),
                Sort.by(Sort.Direction.DESC, "created"));
        itemsDto.forEach(itemDto -> {
            setBookings(itemDto, bookingDtoShorts);
            setComments(itemDto, comments);
        });
        return itemsDto;
    }

    @Transactional(readOnly = true)
    @Override
    public List<ItemDto> searchItems(String text) {
        List<Item> items;
        if (text.isBlank()) {
            items = new ArrayList<>();
        } else {
            items = itemRepository.findByNameOrDescriptionLike(text.toLowerCase());
        }
        return items
                .stream()
                .map(itemMapper::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public void removeItem(long userId, long itemId) {
        userService.getUserById(userId);
        Item item = itemRepository.findById(itemId).orElseThrow(() ->
                new ObjectNotFoundException(String.format("Вещь с id %s не найдена", itemId)));
        itemRepository.deleteById(item.getId());
    }

    @Transactional
    @Override
    public CommentDto addComment(long userId, long itemId, CommentDto commentDto) {
        Comment comment = commentMapper.convertFromDto(commentDto);
        User user = userService.getUserById(userId);
        Item item = itemRepository.findById(itemId).orElseThrow(() -> new ObjectNotFoundException(
                String.format("Вещь с id %s не найдена", itemId)));
        List<Booking> bookings = bookingRepository.findAllByItemIdAndBookerIdAndStatus(itemId, userId, Status.APPROVED,
                Sort.by(Sort.Direction.DESC, "start")).orElseThrow(() -> new ObjectNotFoundException(
                String.format("Пользователь с id %d не арендовал вещь с id %d.", userId, itemId)));
        bookings.stream().filter(booking -> booking.getEnd().isBefore(LocalDateTime.now())).findAny().orElseThrow(() ->
                new ObjectNotAvailableException(String.format("Пользователь с id %d не может оставлять комментарии вещи " +
                        "с id %d.", userId, itemId)));
        comment.setAuthor(user);
        comment.setItem(item);
        comment.setCreated(LocalDateTime.now());
        Comment commentSaved = commentRepository.save(comment);
        return commentMapper.convertToDto(commentSaved);
    }

    private void setBookings(ItemDto itemDto, List<BookingDtoShort> bookings) {
        itemDto.setLastBooking(bookings.stream()
                .filter(b -> (b.getItem().getId() == itemDto.getId()) && !b.getStart().isAfter(LocalDateTime.now()))
                .sorted(Comparator.comparing(BookingDtoShort::getStart).reversed())
                .findFirst()
                .orElse(null));
        itemDto.setNextBooking(bookings.stream()
                .filter(b -> (b.getItem().getId() == itemDto.getId() && itemDto.getId() != 1) && b.getStart().isAfter(LocalDateTime.now()))
                .reduce((a, b) -> a.getStart().isBefore(b.getStart()) ? a : b)
                .orElse(null));
    }

    private void setComments(ItemDto itemDto, List<Comment> comments) {
        itemDto.setComments(comments.stream()
                .filter(comment -> comment.getItem().getId() == itemDto.getId())
                .map(commentMapper::convertToDto)
                .collect(Collectors.toList()));
    }
}