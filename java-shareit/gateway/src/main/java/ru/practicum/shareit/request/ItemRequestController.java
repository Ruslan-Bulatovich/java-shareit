package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import ru.practicum.shareit.common.Header;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

@RestController
@RequestMapping("/requests")
@Validated
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ItemRequestController {
    private final ItemRequestClient itemRequestClient;

    @PostMapping
    public Mono<ResponseEntity<Object>> createRequest(@RequestHeader(Header.userIdHeader) @Min(1) Long requesterId,
                                                      @RequestBody @Valid ItemRequestDto itemRequestDto) {
        return itemRequestClient.createRequest(requesterId, itemRequestDto);
    }

    @GetMapping
    public Mono<ResponseEntity<Object>> getPrivateRequests(
            @RequestHeader(Header.userIdHeader) @Min(1) Long requesterId,
            @RequestParam(value = "from", defaultValue = "0") @Min(0) Integer from,
            @RequestParam(value = "size", defaultValue = "10") @Min(1) @Max(20) Integer size) {
        return itemRequestClient.getPrivateRequests(requesterId, from, size);
    }

    @GetMapping("all")
    public Mono<ResponseEntity<Object>> getOtherRequests(
            @RequestHeader(Header.userIdHeader) @Min(1) Long requesterId,
            @RequestParam(value = "from", defaultValue = "0") @Min(0) Integer from,
            @RequestParam(value = "size", defaultValue = "10") @Min(1) @Max(20) Integer size) {
        return itemRequestClient.getOtherRequests(requesterId, from, size);
    }

    @GetMapping("{requestId}")
    public Mono<ResponseEntity<Object>> getItemRequest(
            @RequestHeader(Header.userIdHeader) @Min(1) Long userId,
            @PathVariable @Min(1) Long requestId) {
        return itemRequestClient.getItemRequest(requestId, userId);
    }
}
