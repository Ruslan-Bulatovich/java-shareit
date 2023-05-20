package ru.practicum.shareit.user.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.exception.DataExistException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping(path = "/users")
@AllArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping()
    public ResponseEntity<UserDto> addUser(@Valid @RequestBody UserDto userDto) throws DataExistException {
        return ResponseEntity.status(201).body(userService.addUser(userDto));
    }

    @GetMapping("{userId}")
    public ResponseEntity<UserDto> getUserById(@PathVariable long userId) {
        return ResponseEntity.ok().body(userService.getUser(userId));
    }

    @GetMapping()
    public ResponseEntity<List<UserDto>> getAllUsers() {
        return ResponseEntity.ok().body(userService.getAllUsers());
    }

    @PatchMapping("{userId}")
    public ResponseEntity<UserDto> updateUser(@PathVariable long userId, @RequestBody UserDto userDto) {
        return ResponseEntity.ok().body(userService.updateUser(userId, userDto));
    }

    @DeleteMapping("{userId}")
    public ResponseEntity<Void> removeUser(@PathVariable long userId) {
        userService.removeUser(userId);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}