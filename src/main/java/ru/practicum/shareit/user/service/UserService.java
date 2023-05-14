package ru.practicum.shareit.user.service;

import ru.practicum.shareit.exception.DataExistException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;

public interface UserService {

    UserDto addUser(UserDto userDto);

    UserDto updateUser(long userId, UserDto userDto) throws DataExistException;

    User getUserById(long userId);

    UserDto getUser(long userId);

    List<UserDto> getAllUsers() throws DataExistException;

    void removeUser(long userId);
}