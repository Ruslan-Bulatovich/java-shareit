package ru.practicum.shareit.item.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.service.ItemService;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

@RestController
@RequestMapping("/items")
@Validated
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ItemController {
    private final ItemService itemService;
    private final String userIdHeader = "X-Sharer-User-Id";

    @PostMapping
    public ResponseEntity<ItemDtoResponse> createItem(@RequestHeader(userIdHeader) @Min(1) Long userId,
                                                      @Valid @RequestBody ItemDto itemDto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(itemService.createItem(itemDto, userId));
    }

    @PatchMapping("{itemId}")
    public ResponseEntity<ItemDtoResponse> updateItem(@RequestHeader(userIdHeader) @Min(1) Long userId,
                                                      @RequestBody ItemDtoUpdate itemDtoUpdate,
                                                      @PathVariable @Min(1) Long itemId) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(itemService.updateItem(itemId, userId, itemDtoUpdate));
    }

    @GetMapping("{itemId}")
    public ResponseEntity<ItemDtoResponse> getItemByItemId(@RequestHeader(userIdHeader) @Min(1) Long userId,
                                                           @PathVariable @Min(1) Long itemId) {
        return ResponseEntity.status(HttpStatus.OK).body(itemService.getItemByItemId(userId, itemId));
    }

    @GetMapping
    public ResponseEntity<ItemListDto> getPersonalItems(
            @RequestHeader(userIdHeader) @Min(1) Long userId,
            @RequestParam(value = "from", defaultValue = "0") @Min(0) Integer from,
            @RequestParam(value = "size", defaultValue = "10") @Min(1) @Max(20) Integer size) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(itemService.getPersonalItems(PageRequest.of(from / size, size), userId));
    }

    @GetMapping("search")
    public ResponseEntity<ItemListDto> getFoundItems(
            @RequestParam String text,
            @RequestParam(value = "from", defaultValue = "0") @Min(0) Integer from,
            @RequestParam(value = "size", defaultValue = "10") @Min(1) @Max(20) Integer size) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(itemService.getFoundItems(PageRequest.of(from / size, size), text));
    }

    @PostMapping("{itemId}/comment")
    public ResponseEntity<CommentDtoResponse> addComment(@PathVariable @Min(1) Long itemId,
                                                         @RequestHeader(userIdHeader) @Min(1) Long userId,
                                                         @Valid @RequestBody CommentDto commentDto) {
        return ResponseEntity.status(HttpStatus.OK).body(itemService.addComment(itemId, userId, commentDto));
    }

}
