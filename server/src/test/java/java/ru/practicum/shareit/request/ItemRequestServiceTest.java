package java.ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import ru.practicum.shareit.error.handler.exception.ObjectNotFoundException;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase
@ActiveProfiles("test")
@Sql(scripts = {"file:src/main/resources/schema.sql"})
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ItemRequestServiceTest {
    private final ItemRequestService itemRequestService;
    private final UserRepository userRepository;
    private User user1;
    private User user2;
    private ItemRequestDto itemRequestDto;

    @BeforeEach
    public void setUp() {
        user1 = new User();
        user1.setName("test name");
        user1.setEmail("test@test.ru");
        user2 = new User();
        user2.setName("test name2");
        user2.setEmail("test2@test.ru");
        itemRequestDto = ItemRequestDto.builder()
                .description("test request description")
                .build();
    }

    @Test
    public void createItemRequest() {
        userRepository.save(user1);
        var savedRequest = itemRequestService.createItemRequest(itemRequestDto, user1.getId());
        var findRequest = itemRequestService.getItemRequest(user1.getId(), savedRequest.getId());
        assertThat(savedRequest).usingRecursiveComparison().ignoringFields("items", "created")
                .isEqualTo(findRequest);
    }

    @Test
    public void createItemRequestWhenRequesterNotFound() {
        userRepository.save(user1);
        assertThatThrownBy(
                () -> itemRequestService.createItemRequest(itemRequestDto, 99L)
        ).isInstanceOf(ObjectNotFoundException.class);
    }

    @Test
    public void getPrivateRequest() {

        userRepository.save(user1);
        userRepository.save(user2);
        var savedRequest = itemRequestService.createItemRequest(itemRequestDto, user2.getId());
        var privateRequests = itemRequestService
                .getPrivateRequests(PageRequest.of(0, 2), user2.getId());
        var findRequest = itemRequestService.getItemRequest(user2.getId(), savedRequest.getId());
        assertThat(privateRequests.getRequests().get(0)).usingRecursiveComparison().isEqualTo(findRequest);
    }

    @Test
    public void getPrivateRequestWhenRequesterNotExistingRequests() {
        userRepository.save(user1);
        assertThatThrownBy(
                () -> itemRequestService
                        .getPrivateRequests(PageRequest.of(0, 2), 55L)
        ).isInstanceOf(ObjectNotFoundException.class);
    }

    @Test
    public void getOtherRequests() {
        //given
        userRepository.save(user1);
        userRepository.save(user2);
        var savedRequest = itemRequestService.createItemRequest(itemRequestDto, user1.getId());
        var findRequest = itemRequestService.getItemRequest(user1.getId(), savedRequest.getId());
        //when
        var otherRequest = itemRequestService.getOtherRequests(PageRequest.of(0, 2), user2.getId());
        //then
        assertThat(otherRequest.getRequests().get(0)).usingRecursiveComparison().isEqualTo(findRequest);
    }

    @Test
    public void getOtherRequestsWhenRequesterNotFound() {
        //given
        userRepository.save(user1);
        itemRequestService.createItemRequest(itemRequestDto, user1.getId());
        assertThatThrownBy(
                //when
                () -> itemRequestService.getOtherRequests(PageRequest.of(0, 2), 50L)
                //then
        ).isInstanceOf(ObjectNotFoundException.class);
    }

    @Test
    public void getItemRequestWhenUserNotFound() {
        //given
        userRepository.save(user1);
        var savedRequest = itemRequestService.createItemRequest(itemRequestDto, user1.getId());
        assertThatThrownBy(
                //when
                () -> itemRequestService.getItemRequest(50L, savedRequest.getId())
                //then
        ).isInstanceOf(ObjectNotFoundException.class);
    }

    @Test
    public void getItemRequestWhenRequestNotFound() {
        //given
        userRepository.save(user1);
        var savedRequest = itemRequestService.createItemRequest(itemRequestDto, user1.getId());
        assertThatThrownBy(
                //when
                () -> itemRequestService.getItemRequest(savedRequest.getId(), 50L)
                //then
        ).isInstanceOf(ObjectNotFoundException.class);
    }
}
