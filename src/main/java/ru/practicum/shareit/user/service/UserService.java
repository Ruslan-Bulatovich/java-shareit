package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserDtoResponse;
import ru.practicum.shareit.user.dto.UserDtoUpdate;
import ru.practicum.shareit.user.dto.UserListDto;

public interface UserService {
    /**
     * Добавление Пользователя
     *
     * @param userDto объект Пользователь
     * @return UserDtoResponse
     */
    UserDtoResponse createUser(UserDto userDto);

    /**
     * Возвращает Пользователя по идентификатору
     *
     * @param userId идентификатор пользователя
     * @return UserDtoResponse
     */
    UserDtoResponse getUserById(Long userId);

    /**
     * Возвращает коллекцию Пользователей
     *
     * @return коллекцию UserListDto
     */
    UserListDto getUsers();

    /**
     * Обновление полей Пользователя
     *
     * @param userId  идентификатор Пользователя
     * @param userDto объект Пользователь с изменениями
     * @return UserDtoResponse
     */
    UserDtoResponse updateUser(UserDtoUpdate userDto, Long userId);

    /**
     * Удаление Пользователя
     *
     * @param userId идентификатор Пользователя
     */
    void deleteUser(Long userId);
}
