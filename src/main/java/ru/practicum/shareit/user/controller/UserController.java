package ru.practicum.shareit.user.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import javax.validation.Valid;
import java.util.Collection;

@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping()
    public ResponseEntity<UserDto> createUser(@Valid @RequestBody UserDto userDto) {
        UserDto userCreated = userService.add(userDto);
        return ResponseEntity.status(201).body(userCreated);
    }

    @GetMapping("{id}")
    public ResponseEntity<UserDto> getUser(@PathVariable Long id) {
        UserDto user = userService.get(id);
        return ResponseEntity.ok().body(user);
    }

    @GetMapping()
    public ResponseEntity<Collection<UserDto>> getAllUsers() {
        Collection<UserDto> users = userService.getAll();
        return ResponseEntity.ok().body(users);
    }

    @PatchMapping("/{userId}")
    public ResponseEntity<UserDto> update(@RequestBody UserDto userDto,
                                          @PathVariable Long userId) {
        UserDto userUpdated = userService.patch(userDto, userId);
        return ResponseEntity.ok().body(userUpdated);
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
