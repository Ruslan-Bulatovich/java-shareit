package ru.practicum.shareit.user.controller;

import lombok.RequiredArgsConstructor;
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

    @PostMapping
    public UserDto create(@Valid @RequestBody UserDto userDto) {

        return userService.add(userDto);
    }

    @GetMapping("{id}")
    public UserDto getUserById(@PathVariable Long id) {

        return userService.get(id);
    }

    @GetMapping
    public Collection<UserDto> getAllUsers() {

        return userService.getAll();
    }

    @PatchMapping("/{userId}")
    public UserDto update(@RequestBody UserDto userDto,
                          @PathVariable Long userId) {
        return userService.patch(userDto, userId);
    }

    @DeleteMapping("{id}")
    public void delete(@PathVariable Long id) {

        userService.delete(id);
    }
}
