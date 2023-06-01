package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import ru.practicum.shareit.booking.enums.Status;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.error.handler.exception.ObjectNotAvailableException;
import ru.practicum.shareit.error.handler.exception.ObjectNotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoResponse;
import ru.practicum.shareit.item.dto.ItemDtoUpdate;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase
@ActiveProfiles("test")
@Sql(scripts = {"file:src/main/resources/schema.sql"})
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ItemServiceIntegrationTest {
    private final ItemService itemService;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final ItemRequestRepository itemRequestRepository;
    private ItemDto item1Dto;
    private ItemDto item2Dto;
    private ItemDtoUpdate item1UpdateDto;
    private User user1;
    private User user2;
    private ItemRequest itemRequest1;
    private Booking lastBooking;
    private Booking nextBooking;


    @BeforeEach
    public void setUp() {
        item1Dto = ItemDto.builder()
                .name("item test")
                .description("item test description")
                .available(Boolean.TRUE)
                .build();
        item2Dto = ItemDto.builder()
                .name("item2 test")
                .description("item2 test description")
                .available(Boolean.TRUE)
                .build();
        item1UpdateDto = ItemDtoUpdate.builder()
                .name("updated name")
                .description("updated description")
                .available(Boolean.FALSE)
                .build();
        user1 = new User();
        user1.setName("test name");
        user1.setEmail("test@test.ru");
        user2 = new User();
        user2.setName("test name2");
        user2.setEmail("test2@test.ru");
        itemRequest1 = new ItemRequest();
        itemRequest1.setDescription("item request1 description");
        itemRequest1.setRequester(user2);
        itemRequest1.setCreated(LocalDateTime.now());
    }

    @Test
    public void createAndGetItemById() {
        userRepository.save(user1);
        var savedItem = itemService.createItem(item1Dto, user1.getId());
        var findItem = itemService.getItemByItemId(user1.getId(), savedItem.getId());
        assertThat(savedItem).usingRecursiveComparison().ignoringFields("comments").isEqualTo(findItem);
    }

    @Test
    public void notExistingUserCreateItem() {
        assertThatThrownBy(
                //then
                () -> itemService.createItem(item1Dto, 1L)
        )
                .isInstanceOf(ObjectNotFoundException.class);
    }

    @Test
    public void createItemWithItemRequest() {
        userRepository.save(user1);
        userRepository.save(user2);
        itemRequestRepository.save(itemRequest1);
        item1Dto.setRequestId(itemRequest1.getId());
        var savedItem = itemService.createItem(item1Dto, user1.getId());
        var findItem = itemService.getItemByItemId(user2.getId(), savedItem.getId());
        var saveRequest = itemRequestRepository.findById(itemRequest1.getId()).get();
        assertThat(savedItem).usingRecursiveComparison().ignoringFields("comments").isEqualTo(findItem);
        assertThat(saveRequest.equals(itemRequest1)).isTrue();
        assertThat(user1.equals(user2)).isFalse();
    }

    @Test
    public void createItemWithNotExistingItemRequest() {
        //given
        userRepository.save(user1);
        userRepository.save(user2);
        itemRequestRepository.save(itemRequest1);
        item1Dto.setRequestId(2L);
        assertThatThrownBy(
                () -> itemService.createItem(item1Dto, user1.getId())
        )
                .isInstanceOf(ObjectNotFoundException.class);
    }

    @Test
    public void updateItem() {
        userRepository.save(user1);
        var savedItem = itemService.createItem(item1Dto, user1.getId());
        var updatedItem = itemService.updateItem(savedItem.getId(), user1.getId(), item1UpdateDto);
        assertThat(updatedItem.getId()).isEqualTo(savedItem.getId());
        assertThat(updatedItem.getName()).isEqualTo(item1UpdateDto.getName());
        assertThat(updatedItem.getDescription()).isEqualTo(item1UpdateDto.getDescription());
        assertThat(updatedItem.getAvailable()).isEqualTo(item1UpdateDto.getAvailable());
    }

    @Test
    public void updateItemWithNotExistingItemId() {
        //given
        userRepository.save(user1);
        itemService.createItem(item1Dto, user1.getId());
        assertThatThrownBy(
                () -> itemService.updateItem(2L, user1.getId(), item1UpdateDto)
        )
                .isInstanceOf(ObjectNotFoundException.class);
    }

    @Test
    public void updateItemWithOtherUser() {
        //given
        userRepository.save(user1);
        //when
        var savedItem = itemService.createItem(item1Dto, user1.getId());
        assertThatThrownBy(
                () -> itemService.updateItem(savedItem.getId(), 2L, item1UpdateDto)
        )
                .isInstanceOf(ObjectNotFoundException.class);
    }

    @Test
    public void getItemByNotExistingId() {
        userRepository.save(user1);
        itemService.createItem(item1Dto, user1.getId());
        assertThatThrownBy(
                () -> itemService.getItemByItemId(user1.getId(), 2L)
        ).isInstanceOf(ObjectNotFoundException.class);
    }

    @Test
    void getItemByIdWithLastAndNextBookings() {

        userRepository.save(user1);
        userRepository.save(user2);
        var savedItem = itemService.createItem(item1Dto, user1.getId());
        createLastAndNextBookings(savedItem);
        bookingRepository.save(lastBooking);
        bookingRepository.save(nextBooking);

        var findItem = itemService.getItemByItemId(user1.getId(), savedItem.getId());

        assertThat(findItem.getId()).isEqualTo(savedItem.getId());
        assertThat(findItem.getName()).isEqualTo(item1Dto.getName());
        assertThat(findItem.getDescription()).isEqualTo(item1Dto.getDescription());
        assertThat(findItem.getAvailable()).isEqualTo(item1Dto.getAvailable());
        assertThat(findItem.getLastBooking().getBookerId()).isEqualTo(user2.getId());
        assertThat(findItem.getLastBooking().getId()).isEqualTo(lastBooking.getId());
        assertThat(findItem.getNextBooking().getBookerId()).isEqualTo(user2.getId());
        assertThat(findItem.getNextBooking().getId()).isEqualTo(nextBooking.getId());
    }

    @Test
    public void getPersonalItems() {

        userRepository.save(user1);
        userRepository.save(user2);
        var savedItem1 = itemService.createItem(item1Dto, user1.getId());
        itemService.createItem(item2Dto, user2.getId());
        createLastAndNextBookings(savedItem1);
        bookingRepository.save(lastBooking);
        bookingRepository.save(nextBooking);
        var findItem = itemService.getItemByItemId(savedItem1.getId(), user1.getId());

        var personalItemsList = itemService.getPersonalItems(PageRequest.of(0, 2), user1.getId());

        assertThat(personalItemsList.getItems()).singleElement().usingRecursiveComparison()
                .ignoringFields("comments").isEqualTo(findItem);
    }

    @Test
    public void getPersonalItemsWithNotExistingUser() {
        userRepository.save(user1);
        userRepository.save(user2);
        var savedItem1 = itemService.createItem(item1Dto, user1.getId());
        itemService.createItem(item2Dto, user2.getId());
        createLastAndNextBookings(savedItem1);
        bookingRepository.save(lastBooking);
        bookingRepository.save(nextBooking);
        assertThatThrownBy(
                () -> itemService.getPersonalItems(PageRequest.of(0, 2), 99L)
        ).isInstanceOf(ObjectNotFoundException.class);
    }

    @Test
    public void getFoundItems() {

        userRepository.save(user1);
        var savedItem1 = itemService.createItem(item1Dto, user1.getId());
        var savedItem2 = itemService.createItem(item2Dto, user1.getId());

        var findItems = itemService.getFoundItems(PageRequest.of(0, 2), "em2");

        assertThat(findItems.getItems()).singleElement().usingRecursiveComparison()
                .ignoringFields("comments").isEqualTo(savedItem2);

        findItems = itemService.getFoundItems(PageRequest.of(0, 2), "test");

        assertThat(findItems.getItems().size()).isEqualTo(2);
        assertThat(findItems.getItems()).element(0).usingRecursiveComparison()
                .ignoringFields("comments").isEqualTo(savedItem1);
        assertThat(findItems.getItems()).element(1).usingRecursiveComparison()
                .ignoringFields("comments").isEqualTo(savedItem2);
    }

    @Test
    public void getFoundItemsWhenSearchTextIsBlank() {

        userRepository.save(user1);
        itemService.createItem(item1Dto, user1.getId());
        itemService.createItem(item2Dto, user1.getId());

        var findItems = itemService.getFoundItems(PageRequest.of(0, 2), " ");

        assertThat(findItems.getItems()).isEmpty();
    }

    @Test
    public void addComment() {

        CommentDto commentDto = CommentDto.builder()
                .text("Nice item, awesome author")
                .build();
        userRepository.save(user1);
        userRepository.save(user2);
        var savedItem1 = itemService.createItem(item1Dto, user1.getId());
        createLastAndNextBookings(savedItem1);
        bookingRepository.save(lastBooking);

        var savedComment1 = itemService.addComment(savedItem1.getId(), user2.getId(), commentDto);
        var comment1 = commentRepository.findById(savedComment1.getId()).get();

        assertThat(savedComment1.getId()).isEqualTo(1L);
        assertThat(savedComment1.getText()).isEqualTo(commentDto.getText());
        assertThat(savedComment1.getCreated()).isBefore(LocalDateTime.now());
        assertThat(savedComment1.getAuthorName()).isEqualTo(user2.getName());

        commentDto.setText("Nice item, awesome author2");
        var savedComment2 = itemService.addComment(savedItem1.getId(), user2.getId(), commentDto);
        var comment2 = commentRepository.findById(savedComment2.getId()).get();

        assertThat(comment1.equals(comment2)).isFalse();

    }

    @Test
    public void addCommentFromUserWithNotExistingBooks() {

        CommentDto commentDto = CommentDto.builder()
                .text("Nice item, awesome author")
                .build();
        userRepository.save(user1);
        userRepository.save(user2);
        var savedItem1 = itemService.createItem(item1Dto, user1.getId());
        assertThatThrownBy(

                () -> itemService.addComment(savedItem1.getId(), user2.getId(), commentDto)

        ).isInstanceOf(ObjectNotAvailableException.class);
    }

    @Test
    public void addCommentForNotExistingItem() {
        CommentDto commentDto = CommentDto.builder()
                .text("Nice item, awesome author")
                .build();
        userRepository.save(user1);
        userRepository.save(user2);
        var savedItem1 = itemService.createItem(item1Dto, user1.getId());
        createLastAndNextBookings(savedItem1);
        bookingRepository.save(lastBooking);
        assertThat(lastBooking.equals(nextBooking)).isFalse();
        assertThatThrownBy(
                () -> itemService.addComment(2L, user2.getId(), commentDto)
        ).isInstanceOf(ObjectNotAvailableException.class);
    }

    @Test
    public void addCommentFromNotExistingUser() {
        CommentDto commentDto = CommentDto.builder()
                .text("Nice item, awesome author")
                .build();
        userRepository.save(user1);
        userRepository.save(user2);
        var savedItem1 = itemService.createItem(item1Dto, user1.getId());
        createLastAndNextBookings(savedItem1);
        bookingRepository.save(lastBooking);
        assertThatThrownBy(
                () -> itemService.addComment(savedItem1.getId(), 50L, commentDto)
        ).isInstanceOf(ObjectNotAvailableException.class);
    }

    private void createLastAndNextBookings(ItemDtoResponse item) {
        Item bookingItem = new Item();
        bookingItem.setId(item.getId());
        bookingItem.setOwner(user1);
        bookingItem.setName(item.getName());
        bookingItem.setDescription(item.getDescription());
        bookingItem.setAvailable(item.getAvailable());
        lastBooking = new Booking();
        lastBooking.setStart(LocalDateTime.now().minusDays(2));
        lastBooking.setEnd(LocalDateTime.now().minusDays(1));
        lastBooking.setItem(bookingItem);
        lastBooking.setBooker(user2);
        lastBooking.setStatus(Status.APPROVED);
        nextBooking = new Booking();
        nextBooking.setStart(LocalDateTime.now().plusDays(1));
        nextBooking.setEnd(LocalDateTime.now().plusDays(2));
        nextBooking.setItem(bookingItem);
        nextBooking.setBooker(user2);
        nextBooking.setStatus(Status.APPROVED);
    }
}