package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.common.Header;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoUpdate;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

@RestController
@RequestMapping("/items")
@Validated
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ItemController {
    private final ItemClient itemClient;

    @PostMapping
    public ResponseEntity<Object> createItem(@RequestHeader(Header.userIdHeader) @Min(1) Long userId,
                                             @Valid @RequestBody ItemDto itemDto) {
        log.info("Create item {} by userId={}", itemDto, userId);
        return itemClient.createItem(userId, itemDto);
    }

    @PatchMapping("{itemId}")
    public ResponseEntity<Object> updateItem(@RequestHeader(Header.userIdHeader) @Min(1) Long userId,
                                             @RequestBody ItemDtoUpdate itemDtoUpdate,
                                             @PathVariable @Min(1) Long itemId) {
        log.info("Update item by itemId={}", itemId);
        return itemClient.updateItem(userId, itemDtoUpdate, itemId);
    }

    @GetMapping("{itemId}")
    public ResponseEntity<Object> getItemByItemId(@RequestHeader(Header.userIdHeader) @Min(1) Long userId,
                                                  @PathVariable @Min(1) Long itemId) {
        log.info("Find items by itemId={}", itemId);
        return itemClient.getItemByItemId(userId, itemId);
    }

    @GetMapping
    public ResponseEntity<Object> getPersonalItems(
            @RequestHeader(Header.userIdHeader) @Min(1) Long userId,
            @RequestParam(value = "from", defaultValue = "0") @Min(0) Integer from,
            @RequestParam(value = "size", defaultValue = "10") @Min(1) @Max(20) Integer size) {
        log.info("Find items by userId={}", userId);
        return itemClient.getPersonalItems(userId, from, size);
    }

    @GetMapping("search")
    public ResponseEntity<Object> getFoundItems(
            @RequestHeader(Header.userIdHeader) @Min(1) Long userId,
            @RequestParam String text,
            @RequestParam(value = "from", defaultValue = "0") @Min(0) Integer from,
            @RequestParam(value = "size", defaultValue = "10") @Min(1) @Max(20) Integer size) {
        log.info("Find items by text '{}'", text);
        return itemClient.getFoundItems(userId, text, from, size);
    }

    @PostMapping("{itemId}/comment")
    public ResponseEntity<Object> addComment(@PathVariable @Min(1) Long itemId,
                                             @RequestHeader(Header.userIdHeader) @Min(1) Long userId,
                                             @Valid @RequestBody CommentDto commentDto) {
        log.info("Create comment by userId={}", userId);
        return itemClient.addComment(itemId, userId, commentDto);
    }

}
