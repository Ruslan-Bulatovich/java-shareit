package ru.practicum.shareit.item.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/items")
@AllArgsConstructor
public class ItemController {
    private final ItemService itemService;
    private final String userIdHeader = "X-Sharer-User-Id";

    @PostMapping()
    public ResponseEntity<ItemDto> addItem(@RequestHeader(userIdHeader) long userId, @Valid @RequestBody ItemDto itemDto) {
        return ResponseEntity.status(201).body(itemService.addItem(userId, itemDto));
    }

    @GetMapping("{itemId}")
    public ResponseEntity<ItemDto> getItem(@PathVariable long itemId, @RequestHeader(userIdHeader) long userId) {
        return ResponseEntity.ok().body(itemService.getItemById(itemId, userId));
    }

    @GetMapping()  // Просмотр владельцем списка всех его вещей с указанием названия и описания для каждой
    public ResponseEntity<List<ItemDto>> getAllItems(@RequestHeader(userIdHeader) long userId) {
        return ResponseEntity.ok().body(itemService.getAllItems(userId));
    }

    @GetMapping("/search")
    public ResponseEntity<List<ItemDto>> searchItems(@RequestParam String text) {
        return ResponseEntity.ok().body(itemService.searchItems(text));
    }

    @PatchMapping("{itemId}")
    public ResponseEntity<ItemDto> updateItem(@RequestHeader(userIdHeader) long userId, @PathVariable long itemId, @RequestBody ItemDto itemDto) {
        return ResponseEntity.ok().body(itemService.updateItem(userId, itemId, itemDto));
    }

    @DeleteMapping("{itemId}")
    public ResponseEntity<Void> removeItem(@RequestHeader(userIdHeader) long userId, @PathVariable long itemId) {
        itemService.removeItem(userId, itemId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<CommentDto> addComment(@RequestHeader(userIdHeader) long userId, @PathVariable long itemId,
                                                 @RequestBody @Valid CommentDto commentDto) {
        return ResponseEntity.ok().body(itemService.addComment(userId, itemId, commentDto));
    }
}