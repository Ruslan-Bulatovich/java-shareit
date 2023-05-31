package ru.practicum.shareit.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.Mockito;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.request.service.ItemRequestServiceImpl;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

public class ItemRequestServiceTest {
    @Mock
    ItemRequestRepository itemRequestRepository;
    @Mock
    UserRepository userRepository;
    @Mock
    ItemRequestMapper itemRequestMapper;
    @Mock
    ItemMapper itemMapper;
    ItemRequestService itemRequestService;

    @BeforeEach
    public void setUp() {
        itemRequestRepository = Mockito.mock(ItemRequestRepository.class);
        userRepository = Mockito.mock(UserRepository.class);
        itemRequestMapper = Mappers.getMapper(ItemRequestMapper.class);
        itemMapper = Mappers.getMapper(ItemMapper.class);
        itemRequestService = new ItemRequestServiceImpl(itemRequestRepository,
                userRepository,
                itemRequestMapper);
    }

    @Test
    public void createItemRequest() {
        User user = new User(1L, "testName", "testEmail@gmail.com");
        ItemRequestDto itemRequestDto = ItemRequestDto.builder()
                .description("test description")
                .build();
        ItemRequest itemRequest = ItemRequest.builder()
                .id(1L)
                .description("test description")
                .created(LocalDateTime.now())
                .requester(user)
                .build();
        when(itemRequestRepository.save(any()))
                .thenReturn(itemRequest);
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        var itemRequestDtoTest = itemRequestService
                .createItemRequest(itemRequestDto, user.getId());

        assertEquals(user.getId(), itemRequestDtoTest.getId());
        assertEquals(itemRequestDto.getDescription(), itemRequestDtoTest.getDescription());
    }
/*
    @Test
    void getPrivateRequest() {
        User userOne = new User(1L, "testNameOne", "testEmailOne@gmail.com");
        User userTwo = new User(2L, "testNameTwo", "testEmailTwo@gmail.com");
        ItemRequestDto itemRequestDto = ItemRequestDto.builder()
                .description("test description")
                .build();
        ItemRequest itemRequest = ItemRequest.builder()
                .id(1L)
                .description("test description")
                .created(LocalDateTime.now())
                .requester(userOne)
                .build();
        when(itemRequestRepository.save(any()))
                .thenReturn(itemRequest);
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        var itemRequestDtoTest = itemRequestService
                .createItemRequest(itemRequestDto, user.getId());
        List<ItemRequestDto> irdList = itemRequestService.findByOwnerId(userOne.getId());

        assertEquals(itemRequestDto.getId(), irdList.get(0).getId());
        assertEquals(itemRequestDto.getDescription(), irdList.get(0).getDescription());
        assertEquals(item.getId(), irdList.get(0).getItems().get(0).getId());
        assertEquals(item.getName(), irdList.get(0).getItems().get(0).getName());
    }

/*

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

 */
}
