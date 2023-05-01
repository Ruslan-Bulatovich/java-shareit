package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemDto;

import java.util.Collection;
import java.util.List;

public interface ItemService {

    ItemDto getItem(Long id);

    List<ItemDto> getAllItemsByUserId(Long userId);

    ItemDto createItem(ItemDto itemDto, Long ownerId);

    ItemDto updateItem(ItemDto itemDto, long itemId, long userId);

    void removeItem(Long id);

    Collection<ItemDto> searchItemsByDescription(String keyword);
}
