package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.NotValidException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemStorage;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserStorage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final UserStorage userStorage;
    private final ItemMapper itemMapper;
    private final ItemStorage itemStorage;

    @Override
    public ItemDto getItem(Long id) {
        itemIdValidator(itemStorage.getItem(id));
        return itemMapper.toItemDto(itemStorage.getItem(id));
    }

    @Override
    public List<ItemDto> getAllItemsByUserId(Long userId) {
        return itemStorage.getAllItems()
                .stream()
                .filter(i -> Objects.equals(i.getOwner().getId(), userId))
                .map(itemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public ItemDto createItem(ItemDto itemDto, Long userId) {
        Item newItem = itemMapper.toItem(itemDto);
        User owner = userStorage.get(userId);
        itemOwnerCheckValidator(owner, newItem, userId);
        Item createdItem = itemStorage.createItem(newItem);
        return itemMapper.toItemDto(createdItem);
    }

    @Override
    public ItemDto updateItem(ItemDto itemDto, long itemId, long userId) {
        Item item = itemMapper.toItem(itemDto);
        userIdValidator(userId);
        Item oldItem = itemStorage.getItem(itemId);
        itemValidator(item, oldItem, userId);
        Item changedItem = itemStorage.updateItem(oldItem);
        return itemMapper.toItemDto(changedItem);
    }

    public void removeItem(Long id) {
        itemIdValidator(itemStorage.getItem(id));
        itemStorage.removeItem(id);
    }

    @Override
    public Collection<ItemDto> searchItemsByDescription(String text) {
        if (text.isBlank()) {
            return new ArrayList<>();
        }
        return itemStorage.getAllItems()
                .stream()
                .filter(i -> i.getDescription().toLowerCase().contains(text.toLowerCase()) && i.getAvailable())
                .map(itemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    private void itemValidator(Item item, Item oldItem, long userId) {
        if (oldItem.getOwner().getId() != userId) {
            throw new NotFoundException("Пользователь не является владельцем вещи");
        }
        if (item.getDescription() != null) {
            oldItem.setDescription(item.getDescription());
        }
        if (item.getAvailable() != null) {
            oldItem.setAvailable(item.getAvailable());
        }
        if (item.getName() != null) {
            oldItem.setName(item.getName());
        }
    }

    private void itemOwnerCheckValidator(User owner, Item newItem, long id) {
        if (owner == null) {
            throw new NotFoundException(String.format("Пользователь id=%d не найден", id));
        } else {
            newItem.setOwner(owner);
        }
    }

    private void itemIdValidator(Item item) {
        if (!itemStorage.getAllItems().contains(itemStorage.getItem(item.getId()))) {
            throw new NotFoundException(String.format("Вещь с id=%d не найдена", itemStorage.getItem(item.getId())));
        }
        if (item.getName().isBlank()) {
            throw new NotValidException("Название вещи не может быть пустым");
        }
        if (item.getDescription().isBlank()) {
            throw new NotValidException("Описание вещи не может быть пустым");
        }
    }

    private void userIdValidator(Long userId) {
        if (!userStorage.getAll().contains(userStorage.get(userId))) {
            throw new NotFoundException(String.format("Пользователь id=%d не найден", userId));
        }
    }
}
