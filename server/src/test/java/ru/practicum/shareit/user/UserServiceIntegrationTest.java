package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.error.handler.exception.ObjectNotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserDtoUpdate;
import ru.practicum.shareit.user.service.UserService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase
@ActiveProfiles("test")
@Sql(scripts = {"file:src/main/resources/schema.sql"})
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UserServiceIntegrationTest {
    private final UserService userService;
    private static UserDto user1;
    private static UserDto user2;
    private static UserDtoUpdate updateUser1;

    @BeforeEach
    public void setUp() {
        user1 = UserDto.builder()
                .name("test name")
                .email("test@test.ru")
                .build();
        user2 = UserDto.builder()
                .name("test name 2")
                .email("test2@test.ru")
                .build();
    }

    @Test
    public void createAndGetUser() {
        var savedUser = userService.createUser(user1);
        var findUser = userService.getUserById(1L);
        assertThat(savedUser).usingRecursiveComparison().isEqualTo(findUser);
    }

    @Test
    public void createUserWithDuplicateEmail() {
        userService.createUser(user1);
        assertThatThrownBy(
                () -> userService.createUser(user1))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    public void getNotExistUserById() {
        assertThatThrownBy(
                () -> userService.getUserById(2L))
                .isInstanceOf(ObjectNotFoundException.class);
    }

    @Test
    public void getEmptyUsersList() {
        var users = userService.getUsers();
        assertThat(users.getUsers()).isEmpty();
    }

    @Test
    public void getUsersList() {
        var savedUser1 = userService.createUser(user1);
        var savedUser2 = userService.createUser(user2);
        var findUsers = userService.getUsers();
        assertThat(findUsers.getUsers()).element(0).usingRecursiveComparison().isEqualTo(savedUser1);
        assertThat(findUsers.getUsers()).element(1).usingRecursiveComparison().isEqualTo(savedUser2);
    }

    @Test
    public void updateUser() {
        updateUser1 = UserDtoUpdate.builder()
                .name("update name")
                .email("update-email@test.ru")
                .build();
        userService.createUser(user1);
        userService.updateUser(updateUser1, 1L);
        var updatedUser1 = userService.getUserById(1L);
        assertThat(updatedUser1.getName()).isEqualTo(updateUser1.getName());
        assertThat(updatedUser1.getEmail()).isEqualTo(updateUser1.getEmail());
    }

    @Test
    public void updateUserName() {
        updateUser1 = UserDtoUpdate.builder()
                .email("update-email@test.ru")
                .build();
        userService.createUser(user1);
        userService.updateUser(updateUser1, 1L);
        var updatedUser1 = userService.getUserById(1L);
        assertThat(updatedUser1.getName()).isEqualTo(user1.getName());
        assertThat(updatedUser1.getEmail()).isEqualTo(updatedUser1.getEmail());
    }

    @Test
    public void updateUserEmail() {
        updateUser1 = UserDtoUpdate.builder()
                .name("update name")
                .build();
        userService.createUser(user1);
        userService.updateUser(updateUser1, 1L);
        var updatedUser1 = userService.getUserById(1L);
        assertThat(updatedUser1.getName()).isEqualTo(updateUser1.getName());
        assertThat(updatedUser1.getEmail()).isEqualTo(user1.getEmail());
    }

    @Test
    @Transactional(propagation = Propagation.SUPPORTS)
    public void updateUserDuplicateEmail() {
        updateUser1 = UserDtoUpdate.builder()
                .email(user1.getEmail())
                .build();
        userService.createUser(user1);
        userService.createUser(user2);
        assertThatThrownBy(
                () -> userService.updateUser(updateUser1, 2L))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    public void deleteUserById() {
        var savedUser = userService.createUser(user1);
        userService.deleteUser(savedUser.getId());
        assertThatThrownBy(() -> userService.getUserById(savedUser.getId())).isInstanceOf(ObjectNotFoundException.class);
    }

    @Test
    public void deleteUserByNotExistId() {
        assertThatThrownBy(
                () -> userService.deleteUser(1L)
        )
                .isInstanceOf(ObjectNotFoundException.class);
    }
}
