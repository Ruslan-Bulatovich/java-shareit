package ru.practicum.shareit.user.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.service.UserService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserDtoResponse;
import ru.practicum.shareit.user.dto.UserDtoUpdate;
import ru.practicum.shareit.user.dto.UserListDto;

import javax.validation.Valid;
import javax.validation.constraints.Min;


@RestController
@RequestMapping("/users")
@Validated
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UserController {
    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserDtoResponse> createUser(@Valid @RequestBody UserDto userDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(userDto));
    }

    @GetMapping("{id}")
    public ResponseEntity<UserDtoResponse> getUserById(@PathVariable("id") @Min(1) Long userId) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.getUserById(userId));
    }

    @GetMapping
    public ResponseEntity<UserListDto> getUsers() {
        return ResponseEntity.status(HttpStatus.OK).body(userService.getUsers());
    }

    @PatchMapping("{id}")
    public ResponseEntity<UserDtoResponse> updateUser(@RequestBody UserDtoUpdate userDtoUpdate,
                                                      @PathVariable("id") Long userId) {
        return ResponseEntity.status(HttpStatus.OK).body(userService.updateUser(userDtoUpdate, userId));
    }

    @DeleteMapping("{id}")
    public void deleteUser(@Min(1) @PathVariable("id") Long userId) {
        userService.deleteUser(userId);
    }

}
