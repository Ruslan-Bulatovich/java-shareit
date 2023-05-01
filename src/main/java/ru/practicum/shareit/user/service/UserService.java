package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.UserDto;

import java.util.Collection;

public interface UserService {

    UserDto get(Long id);

    Collection<UserDto> getAll();

    UserDto add(UserDto userDto);

    UserDto patch(UserDto userDto, Long id);

    Boolean delete(Long id);
}
