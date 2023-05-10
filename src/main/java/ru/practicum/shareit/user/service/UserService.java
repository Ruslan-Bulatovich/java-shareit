package ru.practicum.shareit.user.service;

import ru.practicum.shareit.exception.DataExistException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;

public interface UserService {

    /**
     * Реализует добавление Пользователя в хранилище
     * @param userDto объект Пользователь
     * @return UserDto
     */
    UserDto addUser(UserDto userDto) throws DataExistException;

    /**
     * Реализует обновление полей Пользователя
     * @param userId идентификатор Пользователя
     * @param userDto объект Пользователь с изменениями
     * @return UserDto
     */
    UserDto updateUser(long userId, UserDto userDto) throws DataExistException;

    /**
     * Возвращает Пользователя по идентификатору
     * @param userId идентификатор пользователя
     * @return User
     */
    User getUserById(long userId);

    /**
     * Возвращает Пользователя по идентификатору
     * @param userId идентификатор пользователя
     * @return UserDto
     */
    UserDto getUser(long userId);

    /**
     * Возвращает коллекцию Пользователей
     * @return коллекцию UserDto
     */
    List<UserDto> getAllUsers() throws DataExistException;

    /**
     * Реализует удаление Пользователя из хранилища
     * @param userId идентификатор Пользователя
     */
    void removeUser(long userId);
}