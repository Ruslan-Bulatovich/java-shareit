package ru.practicum.shareit.user.service;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import ru.practicum.shareit.exception.DataExistException;
import ru.practicum.shareit.exception.ObjectNotFoundException;
import ru.practicum.shareit.logger.Logger;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final String host = "localhost";
    private final String port = "8080";
    private final String protocol = "http";

    @Transactional
    @Override
    public UserDto addUser(UserDto userDto) {
        User user = userMapper.convertFromDto(userDto);
        try {
            User userSaved = userRepository.save(user);
            UriComponents uriComponents = UriComponentsBuilder.newInstance()
                    .scheme(protocol)
                    .host(host)
                    .port(port)
                    .path("/users")
                    .build();
            Logger.logSave(HttpMethod.POST, uriComponents.toUriString(), userSaved.toString());
            return userMapper.convertToDto(userSaved);
        } catch (DataExistException e) {
            throw new DataExistException(String.format("Пользователь с email %s уже есть в базе", user.getEmail()));
        }

    }

    @Transactional
    @Override
    public  UserDto updateUser(long id, UserDto userDto) {
        User user = userMapper.convertFromDto(userDto);
        try {
            User targetUser = getUserById(id);
            if (StringUtils.hasLength(user.getEmail())) {
                targetUser.setEmail(user.getEmail());
            }
            if (StringUtils.hasLength(user.getName())) {
                targetUser.setName(user.getName());
            }
            User userSaved = userRepository.save(targetUser);
            UriComponents uriComponents = UriComponentsBuilder.newInstance()
                    .scheme(protocol)
                    .host(host)
                    .port(port)
                    .path("/users/{id}")
                    .build();
            Logger.logSave(HttpMethod.PATCH, uriComponents.toUriString(), userSaved.toString());
            return userMapper.convertToDto(userSaved);
        } catch (DataExistException e) {
            throw new DataExistException(String.format("Пользователь с email %s уже есть в базе", user.getEmail()));
        }
    }

    @Transactional(readOnly = true)
    @Override
    public User getUserById(long userId) {
        User user = userRepository.findById(userId).orElseThrow(() ->
                new ObjectNotFoundException(String.format("Пользователь с id %s не найден", userId)));
        UriComponents uriComponents = UriComponentsBuilder.newInstance()
                .scheme(protocol)
                .host(host)
                .port(port)
                .path("/users/{userId}")
                .build();
        Logger.logSave(HttpMethod.GET, uriComponents.toUriString(), user.toString());
        return user;
    }

    @Transactional(readOnly = true)
    @Override
    public UserDto getUser(long userId) {
        User user = userRepository.findById(userId).orElseThrow(() ->
                new ObjectNotFoundException(String.format("Пользователь с id %s не найден", userId)));
        UriComponents uriComponents = UriComponentsBuilder.newInstance()
                .scheme(protocol)
                .host(host)
                .port(port)
                .path("/users/{userId}")
                .build();
        Logger.logSave(HttpMethod.GET, uriComponents.toUriString(), user.toString());
        return userMapper.convertToDto(user);
    }

    @Transactional(readOnly = true)
    @Override
    public List<UserDto> getAllUsers() {
        List<User> users = userRepository.findAll();
        UriComponents uriComponents = UriComponentsBuilder.newInstance()
                .scheme(protocol)
                .host(host)
                .port(port)
                .path("/users")
                .build();
        Logger.logSave(HttpMethod.GET, uriComponents.toUriString(), users.toString());
        return users
                .stream()
                .map(userMapper::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public void removeUser(long id) {
        userRepository.deleteById(id);
    }
}