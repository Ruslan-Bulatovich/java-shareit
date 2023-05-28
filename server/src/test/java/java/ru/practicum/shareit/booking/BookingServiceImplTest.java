package java.ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoResponse;
import ru.practicum.shareit.booking.dto.BookingListDto;
import ru.practicum.shareit.booking.enums.Status;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.error.handler.exception.InvalidDataException;
import ru.practicum.shareit.error.handler.exception.ObjectNotAvailableException;
import ru.practicum.shareit.error.handler.exception.ObjectNotFoundException;
import ru.practicum.shareit.error.handler.exception.StateException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoResponse;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserDtoResponse;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class BookingServiceImplTest {
    private final BookingService bookingService;
    private final UserService userService;
    private final ItemService itemService;
    private final UserDto testUser = new UserDto("testUser", "test@email.com");
    private final UserDto testUser2 = new UserDto("testUser2", "test2@email.com");
    private final ItemDto testItem = ItemDto.builder().name("testItem").description("testDescription").available(true).build();
    private final ItemDto testItem2 = ItemDto.builder().name("testItem2").description("testDescription2").available(true).build();
    private final BookingDto bookingToCreate = BookingDto.builder().itemId(1L).start(LocalDateTime.now().plusHours(1))
            .end(LocalDateTime.now().plusHours(2)).build();
    private BookingDto currentBookingForItem1;
    private BookingDto currentBookingForItem2;
    private BookingDto futureBookingForItem1;
    private BookingDto futureBookingForItem2;
    private BookingDto pastBookingForItem1;
    private BookingDto pastBookingForItem2;
    private BookingDto rejectedBookingForItem1;
    private BookingDto rejectedBookingForItem2;
    private BookingDto waitingBookingForItem1;
    protected BookingDto waitingBookingForItem2;

    private BookingDtoResponse currentBookingForItem11;
    private BookingDtoResponse currentBookingForItem22;
    private BookingDtoResponse futureBookingForItem11;
    private BookingDtoResponse futureBookingForItem22;
    private BookingDtoResponse pastBookingForItem11;
    private BookingDtoResponse pastBookingForItem22;
    private BookingDtoResponse rejectedBookingForItem11;
    private BookingDtoResponse rejectedBookingForItem22;
    private BookingDtoResponse waitingBookingForItem11;
    protected BookingDtoResponse waitingBookingForItem22;


    @Test
    public void createAndGetBooking() {
        UserDtoResponse createdOwner = userService.createUser(testUser);
        UserDtoResponse createdBooker = userService.createUser(testUser2);
        ItemDtoResponse item = itemService.createItem(testItem, createdOwner.getId());
        BookingDtoResponse createdBooking = bookingService.createBooking(createdBooker.getId(), bookingToCreate);
        assertThat(createdBooking.getId(), equalTo(1L));
        assertThat(createdBooking.getStart(), equalTo(bookingToCreate.getStart()));
        assertThat(createdBooking.getEnd(), equalTo(bookingToCreate.getEnd()));
        assertThat(createdBooking.getItem().getId(), equalTo(item.getId()));
        assertThat(createdBooking.getStatus(), equalTo(Status.WAITING));
    }

    @Test
    public void createBookingWithNotExistingItem() {
        UserDtoResponse createdBooker = userService.createUser(testUser2);
        assertThatThrownBy(
                () -> bookingService.createBooking(createdBooker.getId(), bookingToCreate)
        ).isInstanceOf(ObjectNotFoundException.class);
    }

    @Test
    public void createBookingWhenEndBeforeStart() {
        UserDtoResponse createdOwner = userService.createUser(testUser);
        UserDtoResponse createdBooker = userService.createUser(testUser2);
        ItemDtoResponse item = itemService.createItem(testItem, createdOwner.getId());
        BookingDto bookingWrongTime = BookingDto.builder().itemId(1L).start(LocalDateTime.now())
                .end(LocalDateTime.now().minusHours(2)).build();
        assertThatThrownBy(
                () -> bookingService.createBooking(createdBooker.getId(), bookingWrongTime)
        ).isInstanceOf(InvalidDataException.class);
    }

    @Test
    public void createBookingWhenBookerIsOwner() {
        UserDtoResponse createdOwner = userService.createUser(testUser);
        UserDtoResponse createdBooker = userService.createUser(testUser2);
        ItemDtoResponse itemDto = itemService.createItem(testItem, createdOwner.getId());
        assertThatThrownBy(
                () -> bookingService.createBooking(createdOwner.getId(), bookingToCreate)
        ).isInstanceOf(ObjectNotFoundException.class);
    }

    @Test
    public void createBookingWhenNotExistingBooker() {
        UserDtoResponse createdOwner = userService.createUser(testUser);
        UserDtoResponse createdBooker = userService.createUser(testUser2);
        ItemDtoResponse itemDto = itemService.createItem(testItem, createdOwner.getId());
        assertThatThrownBy(
                () -> bookingService.createBooking(99L, bookingToCreate)
        ).isInstanceOf(ObjectNotFoundException.class);
    }

    @Test
    public void approveBooking() {
        UserDtoResponse createdOwner = userService.createUser(testUser);
        UserDtoResponse createdBooker = userService.createUser(testUser2);
        ItemDtoResponse itemDto = itemService.createItem(testItem, createdOwner.getId());
        var createdBooking = bookingService.createBooking(createdBooker.getId(), bookingToCreate);
        var approvedBooking = bookingService.approveBooking(createdOwner.getId(), createdBooking.getId(), "true");
        var findBooking = bookingService
                .getBookingByIdForOwnerAndBooker(createdBooking.getId(), createdOwner.getId());
        assertThat(approvedBooking).usingRecursiveComparison().isEqualTo(findBooking);
    }

    @Test
    public void rejectBooking() {
        UserDtoResponse createdOwner = userService.createUser(testUser);
        UserDtoResponse createdBooker = userService.createUser(testUser2);
        ItemDtoResponse itemDto = itemService.createItem(testItem, createdOwner.getId());
        var createdBooking = bookingService.createBooking(createdBooker.getId(), bookingToCreate);
        var approvedBooking = bookingService.approveBooking(createdOwner.getId(), createdBooking.getId(), "FALSE");
        var findBooking = bookingService
                .getBookingByIdForOwnerAndBooker(createdBooking.getId(), createdOwner.getId());
        assertThat(approvedBooking).usingRecursiveComparison().isEqualTo(findBooking);
    }

    @Test
    public void approveBookingWithIncorrectParamApproved() {
        UserDtoResponse createdOwner = userService.createUser(testUser);
        UserDtoResponse createdBooker = userService.createUser(testUser2);
        ItemDtoResponse itemDto = itemService.createItem(testItem, createdOwner.getId());
        var createdBooking = bookingService.createBooking(createdBooker.getId(), bookingToCreate);
        assertThatThrownBy(
                () -> bookingService.approveBooking(createdOwner.getId(), createdBooking.getId(), "truee")
        ).isInstanceOf(ObjectNotAvailableException.class);
    }

    @Test
    public void approveBookingWithNotExistingBooking() {
        UserDtoResponse createdOwner = userService.createUser(testUser);
        UserDtoResponse createdBooker = userService.createUser(testUser2);
        ItemDtoResponse itemDto = itemService.createItem(testItem, createdOwner.getId());
        var createdBooking = bookingService.createBooking(createdBooker.getId(), bookingToCreate);
        assertThatThrownBy(
                () -> bookingService.approveBooking(createdOwner.getId(), 99L, "true")
        ).isInstanceOf(ObjectNotFoundException.class);
    }

    @Test
    public void approveBookingWhenUserIsNotOwner() {
        UserDtoResponse createdOwner = userService.createUser(testUser);
        UserDtoResponse createdBooker = userService.createUser(testUser2);
        ItemDtoResponse itemDto = itemService.createItem(testItem, createdOwner.getId());
        var createdBooking = bookingService.createBooking(createdBooker.getId(), bookingToCreate);
        assertThatThrownBy(
                () -> bookingService.approveBooking(createdBooker.getId(), createdBooking.getId(), "true")
        ).isInstanceOf(ObjectNotFoundException.class);
    }

    @Test
    public void getBookingWhenBookingNotFound() {
        UserDtoResponse createdOwner = userService.createUser(testUser);
        UserDtoResponse createdBooker = userService.createUser(testUser2);
        ItemDtoResponse itemDto = itemService.createItem(testItem, createdOwner.getId());
        var createdBooking = bookingService.createBooking(createdBooker.getId(), bookingToCreate);
        assertThatThrownBy(
                () -> bookingService.getBookingByIdForOwnerAndBooker(99L, createdOwner.getId())
        ).isInstanceOf(ObjectNotFoundException.class);
    }

    @Test
    public void getBookingWhenUserIsNotOwnerOrBooker() {
        UserDtoResponse createdOwner = userService.createUser(testUser);
        UserDtoResponse createdBooker = userService.createUser(testUser2);
        ItemDtoResponse itemDto1 = itemService.createItem(testItem, createdOwner.getId());
        var createdBooking = bookingService.createBooking(createdBooker.getId(), bookingToCreate);
        assertThatThrownBy(
                () -> bookingService.getBookingByIdForOwnerAndBooker(createdBooking.getId(), 10L)
        ).isInstanceOf(ObjectNotFoundException.class);
    }

    @Test
    public void getAllBookingForUserWhenStateIsAll() {
        UserDtoResponse user1 = userService.createUser(testUser);
        UserDtoResponse user2 = userService.createUser(testUser2);
        ItemDtoResponse item1 = itemService.createItem(testItem, user1.getId());
        ItemDtoResponse item2 = itemService.createItem(testItem2, user2.getId());
        initializationItem2AndBookings(item1, item2);
        addBookingsInDb(user1, user2);
        BookingListDto findBookingList = bookingService
                .getAllBookingsForUser(PageRequest.of(0, 10), user1.getId(), "ALL");
        List<Long> ids = findBookingList.getBookings().stream().map(BookingDtoResponse::getId).sorted().collect(Collectors.toList());
        assertThat(findBookingList.getBookings().size()).isEqualTo(5);
        assertThat(ids).element(0).isEqualTo(currentBookingForItem22.getId());
        assertThat(ids).element(1).isEqualTo(pastBookingForItem22.getId());
        assertThat(ids).element(2).isEqualTo(futureBookingForItem22.getId());
        assertThat(ids).element(3).isEqualTo(waitingBookingForItem22.getId());
        assertThat(ids).element(4).isEqualTo(rejectedBookingForItem22.getId());
    }

    @Test
    public void getAllBookingsForItemsUser() {
        UserDtoResponse user1 = userService.createUser(testUser);
        UserDtoResponse user2 = userService.createUser(testUser2);
        ItemDtoResponse item1 = itemService.createItem(testItem, user1.getId());
        ItemDtoResponse item2 = itemService.createItem(testItem2, user2.getId());
        initializationItem2AndBookings(item1, item2);
        addBookingsInDb(user1, user2);
        var findBookingList = bookingService
                .getAllBookingsForItemsUser(PageRequest.of(0, 10), user1.getId(), "ALL");
        assertThat(findBookingList.getBookings().size()).isEqualTo(5);
        List<Long> ids = findBookingList.getBookings().stream().map(BookingDtoResponse::getId).sorted().collect(Collectors.toList());
        assertThat(ids).element(0).isEqualTo(currentBookingForItem11.getId());
        assertThat(ids).element(1).isEqualTo(pastBookingForItem11.getId());
        assertThat(ids).element(2).isEqualTo(futureBookingForItem11.getId());
        assertThat(ids).element(3).isEqualTo(waitingBookingForItem11.getId());
        assertThat(ids).element(4).isEqualTo(rejectedBookingForItem11.getId());
    }

    @Test
    public void getAllBookingsForUserWhenStateIsCurrent() {
        UserDtoResponse user1 = userService.createUser(testUser);
        UserDtoResponse user2 = userService.createUser(testUser2);
        ItemDtoResponse item1 = itemService.createItem(testItem, user1.getId());
        ItemDtoResponse item2 = itemService.createItem(testItem2, user2.getId());
        initializationItem2AndBookings(item1, item2);
        addBookingsInDb(user1, user2);
        var findBookingList = bookingService
                .getAllBookingsForUser(PageRequest.of(0, 10), user2.getId(), "CURRENT");
        assertThat(findBookingList.getBookings().size()).isEqualTo(1);
        List<Long> ids = findBookingList.getBookings().stream().map(BookingDtoResponse::getId).sorted().collect(Collectors.toList());
        assertThat(ids).element(0).isEqualTo(currentBookingForItem11.getId());
    }

    @Test
    public void getAllBookingsForItemsUserWhenStateIsCurrent() {
        UserDtoResponse user1 = userService.createUser(testUser);
        UserDtoResponse user2 = userService.createUser(testUser2);
        ItemDtoResponse item1 = itemService.createItem(testItem, user1.getId());
        ItemDtoResponse item2 = itemService.createItem(testItem2, user2.getId());
        initializationItem2AndBookings(item1, item2);
        addBookingsInDb(user1, user2);
        var findBookingList = bookingService
                .getAllBookingsForItemsUser(PageRequest.of(0, 10), user1.getId(), "CURRENT");
        List<Long> ids = findBookingList.getBookings().stream().map(BookingDtoResponse::getId).sorted().collect(Collectors.toList());
        assertThat(ids).singleElement().isEqualTo(currentBookingForItem11.getId());
    }

    @Test
    public void getAllBookingsForUserWhenStateIsPast() {
        UserDtoResponse user1 = userService.createUser(testUser);
        UserDtoResponse user2 = userService.createUser(testUser2);
        ItemDtoResponse item1 = itemService.createItem(testItem, user1.getId());
        ItemDtoResponse item2 = itemService.createItem(testItem2, user2.getId());
        initializationItem2AndBookings(item1, item2);
        addBookingsInDb(user1, user2);
        var findBookingList = bookingService
                .getAllBookingsForUser(PageRequest.of(0, 10), user1.getId(), "PAST");
        assertThat(findBookingList.getBookings().size()).isEqualTo(1);
        List<Long> ids = findBookingList.getBookings().stream().map(BookingDtoResponse::getId).sorted().collect(Collectors.toList());
        assertThat(ids).singleElement().isEqualTo(pastBookingForItem22.getId());
    }

    @Test
    public void getAllBookingsForItemsUserWhenStateIsPast() {
        UserDtoResponse user1 = userService.createUser(testUser);
        UserDtoResponse user2 = userService.createUser(testUser2);
        ItemDtoResponse item1 = itemService.createItem(testItem, user1.getId());
        ItemDtoResponse item2 = itemService.createItem(testItem2, user2.getId());
        initializationItem2AndBookings(item1, item2);
        addBookingsInDb(user1, user2);
        var findBookingList = bookingService
                .getAllBookingsForItemsUser(PageRequest.of(0, 10), user1.getId(), "PAST");
        List<Long> ids = findBookingList.getBookings().stream().map(BookingDtoResponse::getId).sorted().collect(Collectors.toList());
        assertThat(ids).singleElement().isEqualTo(pastBookingForItem11.getId());
    }

    @Test
    public void getAllBookingsForUserWhenStateIsFuture() {
        UserDtoResponse user1 = userService.createUser(testUser);
        UserDtoResponse user2 = userService.createUser(testUser2);
        ItemDtoResponse item1 = itemService.createItem(testItem, user1.getId());
        ItemDtoResponse item2 = itemService.createItem(testItem2, user2.getId());
        initializationItem2AndBookings(item1, item2);
        addBookingsInDb(user1, user2);
        var findBookingList = bookingService
                .getAllBookingsForUser(PageRequest.of(0, 10), user2.getId(), "Future");
        assertThat(findBookingList.getBookings().size()).isEqualTo(3); //2
        List<Long> ids = findBookingList.getBookings().stream().map(BookingDtoResponse::getId).sorted().collect(Collectors.toList());
        assertThat(ids).first().isEqualTo(futureBookingForItem11.getId());
        assertThat(ids).element(1).isEqualTo(waitingBookingForItem11.getId());
        assertThat(ids).element(2).isEqualTo(rejectedBookingForItem11.getId());
    }

    @Test
    public void getAllBookingsForItemsUserWhenStateIsFuture() {
        UserDtoResponse user1 = userService.createUser(testUser);
        UserDtoResponse user2 = userService.createUser(testUser2);
        ItemDtoResponse item1 = itemService.createItem(testItem, user1.getId());
        ItemDtoResponse item2 = itemService.createItem(testItem2, user2.getId());
        initializationItem2AndBookings(item1, item2);
        addBookingsInDb(user1, user2);
        var findBookingList = bookingService
                .getAllBookingsForItemsUser(PageRequest.of(0, 10), user1.getId(), "Future");
        assertThat(findBookingList.getBookings().size()).isEqualTo(3);//1
        List<Long> ids = findBookingList.getBookings().stream().map(BookingDtoResponse::getId).sorted().collect(Collectors.toList());
        assertThat(ids).first().isEqualTo(futureBookingForItem11.getId());
        assertThat(ids).element(1).isEqualTo(waitingBookingForItem11.getId());
        assertThat(ids).element(2).isEqualTo(rejectedBookingForItem11.getId());
    }

    @Test
    public void getAllBookingsForUserWhenStateIsWaiting() {
        UserDtoResponse user1 = userService.createUser(testUser);
        UserDtoResponse user2 = userService.createUser(testUser2);
        ItemDtoResponse item1 = itemService.createItem(testItem, user1.getId());
        ItemDtoResponse item2 = itemService.createItem(testItem2, user2.getId());
        initializationItem2AndBookings(item1, item2);
        addBookingsInDb(user1, user2);
        var findBookingList = bookingService
                .getAllBookingsForUser(PageRequest.of(0, 10), user2.getId(), "waiting");
        assertThat(findBookingList.getBookings().size()).isEqualTo(1);
        List<Long> ids = findBookingList.getBookings().stream().map(BookingDtoResponse::getId).sorted().collect(Collectors.toList());
        assertThat(ids).singleElement().isEqualTo(waitingBookingForItem11.getId());
    }

    @Test
    public void getAllBookingsForItemsUserWhenStateIsWaiting() {
        UserDtoResponse user1 = userService.createUser(testUser);
        UserDtoResponse user2 = userService.createUser(testUser2);
        ItemDtoResponse item1 = itemService.createItem(testItem, user1.getId());
        ItemDtoResponse item2 = itemService.createItem(testItem2, user2.getId());
        initializationItem2AndBookings(item1, item2);
        addBookingsInDb(user1, user2);
        var findBookingList = bookingService
                .getAllBookingsForItemsUser(PageRequest.of(0, 10), user1.getId(), "waiting");
        List<Long> ids = findBookingList.getBookings().stream().map(BookingDtoResponse::getId).sorted().collect(Collectors.toList());
        assertThat(ids).singleElement().isEqualTo(waitingBookingForItem11.getId());
    }

    @Test
    public void getAllBookingsForUserWhenStateIsRejected() {
        UserDtoResponse user1 = userService.createUser(testUser);
        UserDtoResponse user2 = userService.createUser(testUser2);
        ItemDtoResponse item1 = itemService.createItem(testItem, user1.getId());
        ItemDtoResponse item2 = itemService.createItem(testItem2, user2.getId());
        initializationItem2AndBookings(item1, item2);
        addBookingsInDb(user1, user2);
        var findBookingList = bookingService
                .getAllBookingsForUser(PageRequest.of(0, 10), user2.getId(), "rejected");
        List<Long> ids = findBookingList.getBookings().stream().map(BookingDtoResponse::getId).sorted().collect(Collectors.toList());
        assertThat(ids).singleElement().isEqualTo(rejectedBookingForItem11.getId());
    }

    @Test
    public void getAllBookingsForItemsUserWhenStateIsRejected() {
        UserDtoResponse user1 = userService.createUser(testUser);
        UserDtoResponse user2 = userService.createUser(testUser2);
        ItemDtoResponse item1 = itemService.createItem(testItem, user1.getId());
        ItemDtoResponse item2 = itemService.createItem(testItem2, user2.getId());
        initializationItem2AndBookings(item1, item2);
        addBookingsInDb(user1, user2);
        var findBookingList = bookingService
                .getAllBookingsForItemsUser(PageRequest.of(0, 10), user1.getId(), "rejected");
        List<Long> ids = findBookingList.getBookings().stream().map(BookingDtoResponse::getId).sorted().collect(Collectors.toList());
        assertThat(ids).singleElement().isEqualTo(rejectedBookingForItem11.getId());
    }

    @Test
    public void getBookingListWithUnknownState() {
        UserDtoResponse user1 = userService.createUser(testUser);
        UserDtoResponse user2 = userService.createUser(testUser2);
        ItemDtoResponse item1 = itemService.createItem(testItem, user1.getId());
        ItemDtoResponse item2 = itemService.createItem(testItem2, user2.getId());
        initializationItem2AndBookings(item1, item2);
        addBookingsInDb(user1, user2);
        assertThatThrownBy(
                () -> bookingService.getAllBookingsForUser(PageRequest.of(0, 10), user1.getId(), "qwe")
        ).isInstanceOf(StateException.class);
    }

    @Test
    public void getAllBookingsForUserWhenUserNotFound() {
        UserDtoResponse user1 = userService.createUser(testUser);
        UserDtoResponse user2 = userService.createUser(testUser2);
        ItemDtoResponse item1 = itemService.createItem(testItem, user1.getId());
        ItemDtoResponse item2 = itemService.createItem(testItem2, user2.getId());
        initializationItem2AndBookings(item1, item2);
        addBookingsInDb(user1, user2);
        assertThatThrownBy(
                () -> bookingService.getAllBookingsForUser(PageRequest.of(0, 10), 50L, "ALL")
        ).isInstanceOf(ObjectNotFoundException.class);
    }

    @Test
    public void getAllBookingsForItemsUserWhenUserNotFound() {
        UserDtoResponse user1 = userService.createUser(testUser);
        UserDtoResponse user2 = userService.createUser(testUser2);
        ItemDtoResponse item1 = itemService.createItem(testItem, user1.getId());
        ItemDtoResponse item2 = itemService.createItem(testItem2, user2.getId());
        initializationItem2AndBookings(item1, item2);
        addBookingsInDb(user1, user2);
        assertThatThrownBy(
                () -> bookingService.getAllBookingsForItemsUser(PageRequest.of(0, 10), 50L, "ALL")
        ).isInstanceOf(RuntimeException.class);
    }

    @SneakyThrows()
    private void initializationItem2AndBookings(ItemDtoResponse item1, ItemDtoResponse item2) {

        currentBookingForItem1 = BookingDto.builder()
                .itemId(item1.getId())
                .start(LocalDateTime.now().minusDays(1))
                .end(LocalDateTime.now().plusDays(1))
                .build();

        currentBookingForItem2 = BookingDto.builder()
                .itemId(item2.getId())
                .start(LocalDateTime.now().minusDays(1))
                .end(LocalDateTime.now().plusDays(1))
                .build();

        pastBookingForItem1 = BookingDto.builder()
                .start(LocalDateTime.now().minusDays(2))
                .end(LocalDateTime.now().minusDays(1))
                .itemId(item1.getId())
                .build();

        pastBookingForItem2 = BookingDto.builder()
                .start(LocalDateTime.now().minusDays(2))
                .end(LocalDateTime.now().minusDays(1))
                .itemId(item2.getId())
                .build();

        futureBookingForItem1 = BookingDto.builder()
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .itemId(item1.getId())
                .build();


        futureBookingForItem2 = BookingDto.builder()
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .itemId(item2.getId())
                .build();


        waitingBookingForItem1 = BookingDto.builder()
                .start(LocalDateTime.now().plusHours(1))
                .end(LocalDateTime.now().plusHours(2))
                .itemId(item1.getId())
                .build();


        waitingBookingForItem2 = BookingDto.builder()
                .start(LocalDateTime.now().plusHours(1))
                .end(LocalDateTime.now().plusHours(2))
                .itemId(item2.getId())
                .build();


        rejectedBookingForItem1 = BookingDto.builder()
                .start(LocalDateTime.now().plusHours(1))
                .end(LocalDateTime.now().plusHours(2))
                .itemId(item1.getId())
                .build();

        rejectedBookingForItem2 = BookingDto.builder()
                .start(LocalDateTime.now().plusHours(1))
                .end(LocalDateTime.now().plusHours(2))
                .itemId(item2.getId())
                .build();
    }

    @SneakyThrows
    private void addBookingsInDb(UserDtoResponse user1, UserDtoResponse user2) {
        currentBookingForItem11 = bookingService.approveBooking(user1.getId(), bookingService.createBooking(user2.getId(), currentBookingForItem1).getId(), "true");
        currentBookingForItem22 = bookingService.approveBooking(user2.getId(), bookingService.createBooking(user1.getId(), currentBookingForItem2).getId(), "true");
        pastBookingForItem11 = bookingService.approveBooking(user1.getId(), bookingService.createBooking(user2.getId(), pastBookingForItem1).getId(), "true");
        pastBookingForItem22 = bookingService.approveBooking(user2.getId(), bookingService.createBooking(user1.getId(), pastBookingForItem2).getId(), "true");
        futureBookingForItem11 = bookingService.approveBooking(user1.getId(), bookingService.createBooking(user2.getId(), futureBookingForItem1).getId(), "true");
        futureBookingForItem22 = bookingService.approveBooking(user2.getId(), bookingService.createBooking(user1.getId(), futureBookingForItem2).getId(), "true");
        waitingBookingForItem11 = bookingService.createBooking(user2.getId(), waitingBookingForItem1);
        waitingBookingForItem22 = bookingService.createBooking(user1.getId(), waitingBookingForItem2);
        rejectedBookingForItem11 = bookingService.approveBooking(user1.getId(), bookingService.createBooking(user2.getId(), rejectedBookingForItem1).getId(), "false");
        rejectedBookingForItem22 = bookingService.approveBooking(user2.getId(), bookingService.createBooking(user1.getId(), rejectedBookingForItem2).getId(), "false");
    }
}

