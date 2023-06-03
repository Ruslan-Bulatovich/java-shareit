package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserDtoUpdate;

import javax.validation.Valid;
import javax.validation.constraints.Min;

@RestController
@RequestMapping("/users")
@Validated
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UserController {
    private final UserClient userClient;

    @PostMapping
    public ResponseEntity<Object> createUser(@Valid @RequestBody UserDto userDto) {
        log.info("Create user {}", userDto);
        return userClient.createUser(userDto);
    }

    @GetMapping("{id}")
    public ResponseEntity<Object> getUserById(@PathVariable("id") @Min(1) Long userId) {
        log.info("Find user by userId={}", userId);
        return userClient.getUserById(userId);
    }

    @GetMapping
    public ResponseEntity<Object> getUsers() {
        log.info("Find all users");
        return userClient.getUsers();
    }

    @PatchMapping("{id}")
    public ResponseEntity<Object> updateUser(@RequestBody UserDtoUpdate userDtoUpdate,
                                             @PathVariable("id") Long userId) {
        log.info("Update user by userId={}", userId);
        return userClient.updateUser(userDtoUpdate, userId);
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Object> deleteUser(@Min(1) @PathVariable("id") Long userId) {
        log.info("Delete user by userId={}", userId);
        return userClient.deleteUser(userId);
    }
}
