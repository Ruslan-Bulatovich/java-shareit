package ru.practicum.shareit.item.storage;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.model.Item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ItemStorageImpl implements ItemStorage {
    private Long itemId = 0L;
    private final Map<Long, Item> items = new HashMap<>();

    private Long generateId() {
        return ++itemId;
    }

    @Override
    public Item createItem(Item item) {
        item.setId(generateId());
        items.put(item.getId(), item);
        return item;
    }

    @Override
    public Item getItem(Long id) {
        return items.get(id);
    }

    @Override
    public Item updateItem(Item item) {
        items.put(item.getId(), item);
        return items.get(item.getId());
    }

    @Override
    public void removeItem(Long id) {
        items.remove(id);
    }

    @Override
    public List<Item> getAllItems() {
        return new ArrayList<>(items.values());
    }


}
