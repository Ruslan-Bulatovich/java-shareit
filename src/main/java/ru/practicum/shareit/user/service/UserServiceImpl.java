package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserStorage;

import java.util.Collection;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserStorage userStorage;
    private final UserMapper userMapper;

    @Override
    public UserDto add(UserDto userDto) {
        User user = userStorage.add(userMapper.toUser(userDto));
        return userMapper.toUserDto(user);
    }

    @Override
    public UserDto patch(UserDto userDto, Long id) {
        userDto.setId(id);
        return userMapper.toUserDto(userStorage.patch(userMapper.toUser(userDto)));
    }

    @Override
    public UserDto get(Long id) {
        return userMapper.toUserDto(userStorage.get(id));
    }

    @Override
    public Collection<UserDto> getAll() {
        return userStorage.getAll()
                .stream()
                .map(userMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public Boolean delete(Long id) {
        return userStorage.delete(id);
    }
}
