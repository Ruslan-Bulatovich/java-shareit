package ru.practicum.shareit.item.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.logger.Logger;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/items")
@AllArgsConstructor
public class ItemController {
    private final ItemService itemService;
    private final String host = "localhost";
    private final String port = "8080";
    private final String protocol = "http";
    private final String userIdHeader = "X-Sharer-User-Id";

    @PostMapping
    public ResponseEntity<ItemDto> addItem(@RequestHeader(userIdHeader) long userId, @Valid @RequestBody ItemDto itemDto) {
        UriComponents uriComponents = UriComponentsBuilder.newInstance()
                .scheme(protocol)
                .host(host)
                .port(port)
                .path("/items")
                .build();
        Logger.logRequest(HttpMethod.POST, uriComponents.toUriString(), itemDto.toString());
        return ResponseEntity.status(201).body(itemService.addItem(userId, itemDto));
    }

    @GetMapping("{itemId}")
    public ResponseEntity<ItemDto> getItem(@PathVariable long itemId, @RequestHeader(userIdHeader) long userId) {
        UriComponents uriComponents = UriComponentsBuilder.newInstance()
                .scheme(protocol)
                .host(host)
                .port(port)
                .path("/items/{itemId}")
                .build();
        Logger.logRequest(HttpMethod.GET, uriComponents.toUriString(), "пусто");
        return ResponseEntity.ok().body(itemService.getItemById(itemId, userId));
    }

    @GetMapping     // Просмотр владельцем списка всех его вещей с указанием названия и описания для каждой
    public ResponseEntity<List<ItemDto>> getAllItems(@RequestHeader(userIdHeader) long userId) {
        UriComponents uriComponents = UriComponentsBuilder.newInstance()
                .scheme(protocol)
                .host(host)
                .port(port)
                .path("/items")
                .build();
        Logger.logRequest(HttpMethod.GET, uriComponents.toUriString(), "пусто");
        return ResponseEntity.ok().body(itemService.getAllItems(userId));
    }

    @GetMapping("/search")
    public ResponseEntity<List<ItemDto>> searchItems(@RequestParam String text) {
        UriComponents uriComponents = UriComponentsBuilder.newInstance()
                .scheme(protocol)
                .host(host)
                .port(port)
                .path("/items/")
                .query("search?text={text}")
                .build();
        Logger.logRequest(HttpMethod.GET, uriComponents.toUriString(), "пусто");
        return ResponseEntity.ok().body(itemService.searchItems(text));
    }

    @PatchMapping("{itemId}")
    public ResponseEntity<ItemDto> updateItem(@RequestHeader(userIdHeader) long userId, @PathVariable long itemId, @RequestBody ItemDto itemDto) {
        UriComponents uriComponents = UriComponentsBuilder.newInstance()
                .scheme(protocol)
                .host(host)
                .port(port)
                .path("/items/{itemId}")
                .build();
        Logger.logRequest(HttpMethod.PATCH, uriComponents.toUriString(), itemDto.toString());
        return ResponseEntity.ok().body(itemService.updateItem(userId, itemId, itemDto));
    }

    @DeleteMapping("{itemId}")
    public ResponseEntity<Void> removeItem(@RequestHeader(userIdHeader) long userId, @PathVariable long itemId) {
        itemService.removeItem(userId, itemId);
        UriComponents uriComponents = UriComponentsBuilder.newInstance()
                .scheme(protocol)
                .host(host)
                .port(port)
                .path("/items/{itemId}")
                .build();
        Logger.logRequest(HttpMethod.DELETE, uriComponents.toUriString(), "пусто");
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<CommentDto> addComment(@RequestHeader(userIdHeader) long userId, @PathVariable long itemId,
                                 @RequestBody @Valid CommentDto commentDto) {
        UriComponents uriComponents = UriComponentsBuilder.newInstance()
                .scheme(protocol)
                .host(host)
                .port(port)
                .path("/items/{itemId}/comment")
                .build();
        Logger.logRequest(HttpMethod.POST, uriComponents.toUriString(), commentDto.toString());
        return ResponseEntity.ok().body(itemService.addComment(userId, itemId, commentDto));
    }
}