package ru.practicum.shareit.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserDtoResponse;
import ru.practicum.shareit.user.dto.UserDtoUpdate;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.service.UserServiceImpl;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserMapper mapper;
    @InjectMocks
    private UserServiceImpl userService;
    private final UserDto userDto = new UserDto("Mike", "mike@gmail.com");
    private final User user = new User(1L, "Mike", "mike@gmail.com");

    @BeforeEach
    public void setUp() {
        userRepository = Mockito.mock(UserRepository.class);
        mapper = Mappers.getMapper(UserMapper.class);
        userService = new UserServiceImpl(userRepository, mapper);
    }

    @Test
    public void createUser() {
        User user = User.builder()
                .id(1L)
                .name("testName")
                .email("testEmail@gmail.com")
                .build();
        UserDto userDto = UserDto.builder()
                .name("testName")
                .email("testEmail@gmail.com")
                .build();

        Mockito.when(userRepository.save(Mockito.any())).thenReturn(user);
        UserDtoResponse userDtoTest = userService.createUser(userDto);
        assertEquals(user.getId(), userDtoTest.getId());
        assertEquals(user.getName(), userDtoTest.getName());
        assertEquals(user.getEmail(), userDtoTest.getEmail());
    }

    @Test
    void update() {
        User user = User.builder()
                .id(1L)
                .name("testName")
                .email("testEmail@gmail.com")
                .build();
        UserDtoUpdate userDtoForUpdate = UserDtoUpdate.builder()
                .name("newTestName")
                .email("newTestEmail@gmail.com")
                .build();

        Mockito.when(userRepository.save(Mockito.any())).thenReturn(user);
        Mockito.when(userRepository.findById(Mockito.any())).thenReturn(Optional.of(user));

        var updatedUserDtoTest = userService.updateUser(userDtoForUpdate, user.getId());
        assertEquals(user.getId(), updatedUserDtoTest.getId());
        assertEquals(user.getName(), updatedUserDtoTest.getName());
        assertEquals(user.getEmail(), updatedUserDtoTest.getEmail());
    }

    @Test
    void updateWithOnlyNameChange() {
        User user = User.builder()
                .id(1L)
                .name("testName")
                .email("testEmail@gmail.com")
                .build();
        UserDtoUpdate userDtoForUpdate = UserDtoUpdate.builder()
                .name("newTestName")
                .build();

        Mockito.when(userRepository.save(Mockito.any())).thenReturn(user);
        Mockito.when(userRepository.findById(Mockito.any())).thenReturn(Optional.of(user));

        var updatedUserDtoTest = userService.updateUser(userDtoForUpdate, user.getId());
        assertEquals(user.getId(), updatedUserDtoTest.getId());
        assertEquals(user.getName(), updatedUserDtoTest.getName());
        assertEquals(user.getEmail(), updatedUserDtoTest.getEmail());
    }

    @Test
    void findById() {
        User user = User.builder()
                .id(1L)
                .name("testName")
                .email("testEmail@gmail.com")
                .build();

        Mockito.when(userRepository.findById(Mockito.any())).thenReturn(Optional.of(user));
        var userDtoTest = userService.getUserById(user.getId());
        assertEquals(user.getId(), userDtoTest.getId());
        assertEquals(user.getName(), userDtoTest.getName());
        assertEquals(user.getEmail(), userDtoTest.getEmail());
    }

    @Test
    void findAll() {
        User userOne = new User(1L, "testNameOne", "testEmailOne@gmail.com");
        User userTwo = new User(2L, "testNameTwo", "testEmailTwo@gmail.com");
        List<User> userList = List.of(userOne, userTwo);

        Mockito.when(userRepository.findAll()).thenReturn(userList);

        List<UserDtoResponse> userDtoList = userService.getUsers().getUsers();
        assertEquals(userList.size(), userDtoList.size());
        assertEquals(userList.get(0).getId(), userDtoList.get(0).getId());
        assertEquals(userList.get(0).getName(), userDtoList.get(0).getName());
        assertEquals(userList.get(0).getEmail(), userDtoList.get(0).getEmail());
    }
}


