package ru.practicum.shareit.item.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.service.ItemService;

@RestController
@RequestMapping("/items")

@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ItemController {
    private final ItemService itemService;
    private static final String userIdHeader = "X-Sharer-User-Id";

    @PostMapping
    public ResponseEntity<ItemDtoResponse> createItem(@RequestHeader(userIdHeader) Long userId,
                                                      @RequestBody ItemDto itemDto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(itemService.createItem(itemDto, userId));
    }

    @PatchMapping("{itemId}")
    public ResponseEntity<ItemDtoResponse> updateItem(@RequestHeader(userIdHeader) Long userId,
                                                      @RequestBody ItemDtoUpdate itemDtoUpdate,
                                                      @PathVariable Long itemId) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(itemService.updateItem(itemId, userId, itemDtoUpdate));
    }

    @GetMapping("{itemId}")
    public ResponseEntity<ItemDtoResponse> getItemByItemId(@RequestHeader(userIdHeader) Long userId,
                                                           @PathVariable Long itemId) {
        return ResponseEntity.status(HttpStatus.OK).body(itemService.getItemByItemId(userId, itemId));
    }

    @GetMapping
    public ResponseEntity<ItemListDto> getPersonalItems(
            @RequestHeader(userIdHeader) Long userId,
            @RequestParam(value = "from", defaultValue = "0") Integer from,
            @RequestParam(value = "size", defaultValue = "10") Integer size) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(itemService.getPersonalItems(PageRequest.of(from / size, size), userId));
    }

    @GetMapping("search")
    public ResponseEntity<ItemListDto> getFoundItems(
            @RequestParam String text,
            @RequestParam(value = "from", defaultValue = "0") Integer from,
            @RequestParam(value = "size", defaultValue = "10") Integer size) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(itemService.getFoundItems(PageRequest.of(from / size, size), text));
    }

    @PostMapping("{itemId}/comment")
    public ResponseEntity<CommentDtoResponse> addComment(@PathVariable Long itemId,
                                                         @RequestHeader(userIdHeader) Long userId,
                                                         @RequestBody CommentDto commentDto) {
        return ResponseEntity.status(HttpStatus.OK).body(itemService.addComment(itemId, userId, commentDto));
    }

}
