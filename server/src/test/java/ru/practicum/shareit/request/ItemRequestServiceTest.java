package ru.practicum.shareit.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.Mockito;
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

    ItemRequestService itemRequestService;

    @BeforeEach
    public void setUp() {
        itemRequestRepository = Mockito.mock(ItemRequestRepository.class);
        userRepository = Mockito.mock(UserRepository.class);
        itemRequestMapper = Mappers.getMapper(ItemRequestMapper.class);
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

}
