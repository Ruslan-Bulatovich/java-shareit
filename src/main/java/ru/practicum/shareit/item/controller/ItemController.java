package ru.practicum.shareit.item.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;
    private final String userIdHeader = "X-Sharer-User-Id";

    @PostMapping()
    public ResponseEntity<ItemDto> createItem(@Valid @RequestBody ItemDto itemDto, @RequestHeader(userIdHeader) Long userId) {
        ItemDto itemCreated = itemService.createItem(itemDto, userId);
        return ResponseEntity.status(201).body(itemCreated);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<ItemDto> updateItem(@RequestBody ItemDto itemDto, @PathVariable Long itemId,
                                              @RequestHeader(userIdHeader) Long userId) {
        ItemDto itemUpdated = itemService.updateItem(itemDto, itemId, userId);
        return ResponseEntity.ok().body(itemUpdated);
    }

    @GetMapping()
    public ResponseEntity<List<ItemDto>> findAll(@RequestHeader(userIdHeader) Long userId) {
        List<ItemDto> items = itemService.getAllItemsByUserId(userId);
        return ResponseEntity.ok().body(items);
    }

    @GetMapping("/search")
    public ResponseEntity<Collection<ItemDto>> searchItems(@RequestParam(name = "text") String text) {
        return ResponseEntity.ok().body(itemService.searchItemsByDescription(text));
    }

    @DeleteMapping("/{itemId}")
    public ResponseEntity<Void> removeItem(@PathVariable Long itemId) {
        itemService.removeItem(itemId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<ItemDto> getItem(@PathVariable Long itemId) {
        ItemDto item = itemService.getItem(itemId);
        return ResponseEntity.ok().body(item);
    }
}
