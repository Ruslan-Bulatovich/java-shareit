package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.error.handler.exception.ObjectNotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoUpdate;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.item.service.ItemServiceImpl;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

public class ItemServiceTest {
    @Mock
    ItemRepository itemRepository;
    @Mock
    UserRepository userRepository;
    @Mock
    BookingRepository bookingRepository;
    @Mock
    CommentRepository commentRepository;
    @Mock
    ItemRequestRepository itemRequestRepository;
    @Mock
    ItemMapper itemMapper;
    ItemService itemService;

    private ItemDto itemDto1;
    private ItemDto itemDto2;
    private Item item1;
    private Item item2;

    private ItemDtoUpdate item1UpdateDto;

    private User user1;
    private User user2;
    private ItemRequest itemRequest1;


    @BeforeEach
    public void setUp() {
        itemRepository = Mockito.mock(ItemRepository.class);
        userRepository = Mockito.mock(UserRepository.class);
        bookingRepository = Mockito.mock(BookingRepository.class);
        commentRepository = Mockito.mock(CommentRepository.class);
        itemRequestRepository = Mockito.mock(ItemRequestRepository.class);
        itemMapper = Mappers.getMapper(ItemMapper.class);
        itemService = new ItemServiceImpl(itemRepository,
                userRepository,
                bookingRepository,
                commentRepository,
                itemMapper,
                itemRequestRepository);

        user1 = User.builder()
                .id(1L)
                .name("test name")
                .email("test@test.ru")
                .build();

        user2 = User.builder()
                .id(1L)
                .name("test name2")
                .email("test2@test.ru")
                .build();

        UserDto userDto1 = UserDto.builder()
                .name("test name")
                .email("test@test.ru")
                .build();
        UserDto userDto2 = UserDto.builder()
                .name("test name2")
                .email("test2@test.ru")
                .build();

        itemDto1 = ItemDto.builder()
                .name("item test")
                .description("item test description")
                .available(Boolean.TRUE)
                .build();
        itemDto2 = ItemDto.builder()
                .name("item2 test")
                .description("item2 test description")
                .available(Boolean.TRUE)
                .build();
        item1 = Item.builder()
                .id(1L)
                .name("item test")
                .description("item test description")
                .available(Boolean.TRUE)
                .owner(user1)
                .build();
        item2 = Item.builder()
                .id(2L)
                .name("item2 test")
                .description("item2 test description")
                .available(Boolean.TRUE)
                .owner(user2)
                .build();

        item1UpdateDto = ItemDtoUpdate.builder()
                .name("updated name")
                .description("updated description")
                .available(Boolean.FALSE)
                .build();

        itemRequest1 = new ItemRequest();
        itemRequest1.setDescription("item request1 description");
        itemRequest1.setRequester(user2);
        itemRequest1.setCreated(LocalDateTime.now());


    }

    @Test
    public void createItemById() {

        Mockito.when(userRepository.save(Mockito.any())).thenReturn(user1);
        when(userRepository.findById(any())).thenReturn(Optional.ofNullable(user1));
        when(itemRepository.save(any())).thenReturn(item1);
        var savedItem = itemService.createItem(itemDto1, user1.getId());
        assertEquals(itemDto1.getName(), savedItem.getName());
        assertEquals(itemDto1.getAvailable(), savedItem.getAvailable());
        assertEquals(itemDto1.getDescription(), savedItem.getDescription());
    }

    @Test
    public void notExistingUserCreateItem() {
        assertThatThrownBy(
                () -> itemService.createItem(itemDto1, 1L)
        )
                .isInstanceOf(ObjectNotFoundException.class);
    }

    @Test
    public void updateItem() {
        Mockito.when(userRepository.save(Mockito.any())).thenReturn(user1);
        when(userRepository.findById(any())).thenReturn(Optional.ofNullable(user1));
        when(itemRepository.save(any())).thenReturn(item1);
        when(itemRepository.findById(any())).thenReturn(Optional.ofNullable(item1));
        var savedItem = itemService.createItem(itemDto1, user1.getId());
        var updatedItem = itemService.updateItem(savedItem.getId(), user1.getId(), item1UpdateDto);
        assertThat(updatedItem.getId()).isEqualTo(savedItem.getId());
        assertThat(updatedItem.getName()).isEqualTo(item1UpdateDto.getName());
        assertThat(updatedItem.getDescription()).isEqualTo(item1UpdateDto.getDescription());
        assertThat(updatedItem.getAvailable()).isEqualTo(item1UpdateDto.getAvailable());

    }

    @Test
    public void updateItemWithNotExistingItemId() {
        Mockito.when(userRepository.save(Mockito.any())).thenReturn(user1);
        when(userRepository.findById(any())).thenReturn(Optional.ofNullable(user1));
        when(itemRepository.save(any())).thenReturn(item1);
        var savedItem = itemService.createItem(itemDto1, user1.getId());
        assertThatThrownBy(
                () -> itemService.updateItem(2L, user1.getId(), item1UpdateDto)
        )
                .isInstanceOf(ObjectNotFoundException.class);
    }

    @Test
    public void getItemById() {
        Mockito.when(userRepository.save(Mockito.any())).thenReturn(user1);
        when(userRepository.findById(any())).thenReturn(Optional.ofNullable(user1));
        when(itemRepository.save(any())).thenReturn(item1);
        when(itemRepository.findById(any())).thenReturn(Optional.ofNullable(item1));
        var savedItem = itemService.createItem(itemDto1, user1.getId());
        var item = itemService.getItemByItemId(user1.getId(), 1L);
        assertEquals(item.getName(), savedItem.getName());
        assertEquals(item.getAvailable(), savedItem.getAvailable());
        assertEquals(item.getDescription(), savedItem.getDescription());
    }

    @Test
    public void getItemByNotExistingId() {
        Mockito.when(userRepository.save(Mockito.any())).thenReturn(user1);
        when(userRepository.findById(any())).thenReturn(Optional.ofNullable(user1));
        when(itemRepository.save(any())).thenReturn(item1);
        var savedItem = itemService.createItem(itemDto1, user1.getId());
        assertThatThrownBy(
                () -> itemService.getItemByItemId(user1.getId(), 2L)
        ).isInstanceOf(ObjectNotFoundException.class);
    }

    @Test
    public void getPersonalItems() {
        Mockito.when(userRepository.save(Mockito.any())).thenReturn(user2);
        when(userRepository.findById(any())).thenReturn(Optional.ofNullable(user2));
        when(userRepository.existsById(any())).thenReturn(true);
        when(itemRepository.save(any())).thenReturn(item2);
        when(itemRepository.findById(any())).thenReturn(Optional.ofNullable(item2));
        var itemUser2 = itemService.createItem(itemDto2, user2.getId());
        var savedItem = itemService.getItemByItemId(itemUser2.getId(), user2.getId());
        when(itemRepository.findAllByOwnerId(any(), anyLong())).thenReturn(List.of(item2));
        var personalItemsList = itemService.getPersonalItems(PageRequest.of(0, 2), 2L);
        var itemUser = personalItemsList.getItems().get(0);
        assertEquals(itemUser.getName(), savedItem.getName());
        assertEquals(itemUser.getAvailable(), savedItem.getAvailable());
        assertEquals(itemUser.getDescription(), savedItem.getDescription());

    }

    @Test
    public void getFoundItems() {
        Mockito.when(userRepository.save(Mockito.any())).thenReturn(user1);
        when(userRepository.findById(any())).thenReturn(Optional.ofNullable(user1));
        when(userRepository.existsById(any())).thenReturn(true);
        when(itemRepository.save(any())).thenReturn(item1);
        when(itemRepository.findById(any())).thenReturn(Optional.ofNullable(item1));
        var savedItem = itemService.createItem(itemDto1, user1.getId());
        when(itemRepository.findAllByNameOrDescriptionContainingIgnoreCaseAndAvailableTrue(any(), anyString(), anyString())).thenReturn(List.of());
        var findItems = itemService.getFoundItems(PageRequest.of(0, 2), "em2");
        assertThat(findItems.getItems().size()).isEqualTo(0);
        when(itemRepository.findAllByNameOrDescriptionContainingIgnoreCaseAndAvailableTrue(any(), anyString(), anyString())).thenReturn((List.of(item1)));
        findItems = itemService.getFoundItems(PageRequest.of(0, 2), "test");
        var itemUser = findItems.getItems().get(0);
        assertEquals(itemUser.getName(), savedItem.getName());
        assertEquals(itemUser.getAvailable(), savedItem.getAvailable());
        assertEquals(itemUser.getDescription(), savedItem.getDescription());
    }

    @Test
    public void addComment() {
        CommentDto commentDto = CommentDto.builder()
                .text("Nice item, awesome author")
                .build();
        Comment comment = Comment.builder()
                .id(1L)
                .text("Nice item, awesome author")
                .item(item1)
                .author(user2)
                .created(LocalDateTime.now())
                .build();
        Mockito.when(userRepository.save(Mockito.any())).thenReturn(user1);
        when(userRepository.findById(any())).thenReturn(Optional.ofNullable(user1));
        when(itemRepository.save(any())).thenReturn(item1);
        when(itemRepository.findById(any())).thenReturn(Optional.ofNullable(item1));
        var savedItem1 = itemService.createItem(itemDto1, user1.getId());
        Mockito.when(userRepository.save(Mockito.any())).thenReturn(user2);
        when(userRepository.findById(any())).thenReturn(Optional.ofNullable(user2));
        when(bookingRepository.existsBookingByItemIdAndBookerIdAndStatusAndEndIsBefore(any(), any(), any(), any())).thenReturn(Boolean.TRUE);
        Mockito.when(commentRepository.save(Mockito.any())).thenReturn(comment);
        var savedComment1 = itemService.addComment(savedItem1.getId(), 2L, commentDto);

        assertThat(savedComment1.getId()).isEqualTo(1L);
        assertThat(savedComment1.getText()).isEqualTo(commentDto.getText());
        assertThat(savedComment1.getCreated()).isBefore(LocalDateTime.now());
        assertThat(savedComment1.getAuthorName()).isEqualTo(user2.getName());
    }
}
