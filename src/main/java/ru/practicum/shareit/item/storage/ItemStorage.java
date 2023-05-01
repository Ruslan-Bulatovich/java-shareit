package ru.practicum.shareit.item.storage;

import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemStorage {

    Item getItem(Long id);

    List<Item> getAllItems();

    Item createItem(Item item);

    Item updateItem(Item item);

    void removeItem(Long id);
}
