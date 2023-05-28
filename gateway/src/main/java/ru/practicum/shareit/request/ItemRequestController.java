package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
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
    public ResponseEntity<Object> createRequest(@RequestHeader(Header.userIdHeader) @Min(1) Long requesterId,
                                                @RequestBody @Valid ItemRequestDto itemRequestDto) {
        return itemRequestClient.createRequest(requesterId, itemRequestDto);
    }

    @GetMapping
    public ResponseEntity<Object> getPrivateRequests(
            @RequestHeader(Header.userIdHeader) @Min(1) Long requesterId,
            @RequestParam(value = "from", defaultValue = "0") @Min(0) Integer from,
            @RequestParam(value = "size", defaultValue = "10") @Min(1) @Max(20) Integer size) {
        return itemRequestClient.getPrivateRequests(requesterId, from, size);
    }

    @GetMapping("all")
    public ResponseEntity<Object> getOtherRequests(
            @RequestHeader(Header.userIdHeader) @Min(1) Long requesterId,
            @RequestParam(value = "from", defaultValue = "0") @Min(0) Integer from,
            @RequestParam(value = "size", defaultValue = "10") @Min(1) @Max(20) Integer size) {
        return itemRequestClient.getOtherRequests(requesterId, from, size);
    }

    @GetMapping("{requestId}")
    public ResponseEntity<Object> getItemRequest(
            @RequestHeader(Header.userIdHeader) @Min(1) Long userId,
            @PathVariable @Min(1) Long requestId) {
        return itemRequestClient.getItemRequest(requestId, userId);
    }
}
