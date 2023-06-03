package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoResponse;
import ru.practicum.shareit.booking.enums.Status;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.error.handler.exception.InvalidDataException;
import ru.practicum.shareit.error.handler.exception.ObjectNotAvailableException;
import ru.practicum.shareit.error.handler.exception.ObjectNotFoundException;
import ru.practicum.shareit.error.handler.exception.StateException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("test")
@Sql(scripts = {"file:src/main/resources/schema.sql"})
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BookingServiceIntegrationTest {
    private final BookingService bookingService;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private User user1;
    private User user2;
    private Item item1;
    private Item item2;
    private BookingDto booking1Dto;
    private Booking currentBookingForItem1;
    private Booking currentBookingForItem2;
    private Booking futureBookingForItem1;
    private Booking futureBookingForItem2;
    private Booking pastBookingForItem1;
    private Booking pastBookingForItem2;
    private Booking rejectedBookingForItem1;
    private Booking rejectedBookingForItem2;
    private Booking waitingBookingForItem1;
    protected Booking waitingBookingForItem2;


    @BeforeEach
    public void setUp() {
        user1 = new User();
        user1.setName("test name");
        user1.setEmail("test@test.ru");
        user2 = new User();
        user2.setName("test name2");
        user2.setEmail("test2@test.ru");
        item1 = new Item();
        item1.setName("test item");
        item1.setDescription("test item description");
        item1.setAvailable(Boolean.TRUE);
        item1.setOwner(user1);
        item2 = new Item();
        item2.setName("test item2");
        item2.setDescription("test item2 description");
        item2.setAvailable(Boolean.TRUE);
        item2.setOwner(user2);
        booking1Dto = BookingDto.builder()
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .itemId(1L)
                .build();
    }

    @Test
    public void createAndGetBooking() {
        userRepository.save(user1);
        userRepository.save(user2);
        itemRepository.save(item1);
        var savedBooking = bookingService.createBooking(user2.getId(), booking1Dto);
        var findBooking = bookingService
                .getBookingByIdForOwnerAndBooker(savedBooking.getId(), user2.getId());
        assertThat(savedBooking).usingRecursiveComparison().ignoringFields("start", "end")
                .isEqualTo(findBooking);
    }

    @Test
    public void createBookingWithNotExistingItem() {
        booking1Dto.setItemId(2L);
        userRepository.save(user1);
        userRepository.save(user2);
        itemRepository.save(item1);
        assertThatThrownBy(
                () -> bookingService.createBooking(user2.getId(), booking1Dto)
        ).isInstanceOf(ObjectNotFoundException.class);
    }

    @Test
    public void createBookingWhenBookerIsOwner() {
        userRepository.save(user1);
        userRepository.save(user2);
        itemRepository.save(item1);
        assertThatThrownBy(
                () -> bookingService.createBooking(user1.getId(), booking1Dto)
        ).isInstanceOf(ObjectNotFoundException.class);
    }

    @Test
    public void createBookingWhenNotExistingBooker() {
        userRepository.save(user1);
        userRepository.save(user2);
        itemRepository.save(item1);
        assertThatThrownBy(
                () -> bookingService.createBooking(99L, booking1Dto)
        ).isInstanceOf(ObjectNotFoundException.class);
    }

    @Test
    public void createBookingWithNotAvailableItem() {
        item1.setAvailable(Boolean.FALSE);
        userRepository.save(user1);
        userRepository.save(user2);
        itemRepository.save(item1);
        assertThatThrownBy(
                () -> bookingService.createBooking(user2.getId(), booking1Dto)
        ).isInstanceOf(ObjectNotAvailableException.class);
    }

    @Test
    public void approveBooking() {
        userRepository.save(user1);
        userRepository.save(user2);
        itemRepository.save(item1);
        var savedBooking = bookingService.createBooking(user2.getId(), booking1Dto);
        var approvedBooking = bookingService
                .approveBooking(user1.getId(), savedBooking.getId(), "true");
        var findBooking = bookingService
                .getBookingByIdForOwnerAndBooker(savedBooking.getId(), user2.getId());
        assertThat(approvedBooking).usingRecursiveComparison().isEqualTo(findBooking);
    }

    @Test
    public void rejectBooking() {
        userRepository.save(user1);
        userRepository.save(user2);
        itemRepository.save(item1);
        var savedBooking = bookingService.createBooking(user2.getId(), booking1Dto);
        var approvedBooking = bookingService
                .approveBooking(user1.getId(), savedBooking.getId(), "FALSE");
        var findBooking = bookingService
                .getBookingByIdForOwnerAndBooker(savedBooking.getId(), user2.getId());
        assertThat(approvedBooking).usingRecursiveComparison().isEqualTo(findBooking);
    }

    @Test
    public void approveBookingWithIncorrectParamApproved() {
        userRepository.save(user1);
        userRepository.save(user2);
        itemRepository.save(item1);
        var savedBooking = bookingService.createBooking(user2.getId(), booking1Dto);
        assertThatThrownBy(
                () -> bookingService.approveBooking(user1.getId(), savedBooking.getId(), "truee")
        ).isInstanceOf(ObjectNotAvailableException.class);
    }

    @Test
    public void approveBookingWithNotExistingBooking() {
        userRepository.save(user1);
        userRepository.save(user2);
        itemRepository.save(item1);
        bookingService.createBooking(user2.getId(), booking1Dto);
        assertThatThrownBy(
                () -> bookingService.approveBooking(user1.getId(), 99L, "true")
        ).isInstanceOf(ObjectNotFoundException.class);
    }

    @Test
    public void approveBookingWhenBookingIsNotWaiting() {
        userRepository.save(user1);
        userRepository.save(user2);
        itemRepository.save(item1);
        var savedBooking = bookingService.createBooking(user2.getId(), booking1Dto);
        bookingService.approveBooking(user1.getId(), savedBooking.getId(), "false");
        assertThatThrownBy(
                () -> bookingService.approveBooking(user1.getId(), savedBooking.getId(), "true")
        ).isInstanceOf(ObjectNotAvailableException.class);
    }

    @Test
    public void approveBookingWhenUserIsNotOwner() {
        userRepository.save(user1);
        userRepository.save(user2);
        itemRepository.save(item1);
        var savedBooking = bookingService.createBooking(user2.getId(), booking1Dto);
        assertThatThrownBy(
                () -> bookingService.approveBooking(user2.getId(), savedBooking.getId(), "true")
        ).isInstanceOf(ObjectNotFoundException.class);
    }

    @Test
    public void getBookingWhenBookingNotFound() {
        userRepository.save(user1);
        userRepository.save(user2);
        itemRepository.save(item1);
        bookingService.createBooking(user2.getId(), booking1Dto);
        assertThatThrownBy(
                () -> bookingService.getBookingByIdForOwnerAndBooker(99L, user2.getId())
        ).isInstanceOf(ObjectNotFoundException.class);
    }

    @Test
    public void getBookingWhenUserIsNotOwnerOrBooker() {
        userRepository.save(user1);
        userRepository.save(user2);
        itemRepository.save(item1);
        var savedBooking = bookingService.createBooking(user2.getId(), booking1Dto);
        assertThatThrownBy(
                () -> bookingService.getBookingByIdForOwnerAndBooker(savedBooking.getId(), 10L)
        ).isInstanceOf(ObjectNotFoundException.class);
    }

    @Test
    public void getAllBookingForUserWhenStateIsAll() {
        initializationItem2AndBookings();
        userRepository.save(user1);
        userRepository.save(user2);
        itemRepository.save(item1);
        itemRepository.save(item2);
        addBookingsInDb();
        var findBookingList = bookingService
                .getAllBookingsForUser(PageRequest.of(0, 10), user2.getId(), "ALL");
        List<Long> ids = findBookingList.getBookings().stream().map(BookingDtoResponse::getId).sorted().collect(Collectors.toList());
        assertThat(findBookingList.getBookings().size()).isEqualTo(10);
        assertThat(ids).first().isEqualTo(pastBookingForItem1.getId());
        assertThat(ids).element(1).isEqualTo(pastBookingForItem2.getId());
        assertThat(ids).element(2).isEqualTo(currentBookingForItem1.getId());
        assertThat(ids).element(3).isEqualTo(currentBookingForItem2.getId());
        assertThat(ids).element(4).isEqualTo(futureBookingForItem1.getId());
        assertThat(ids).element(5).isEqualTo(futureBookingForItem2.getId());
        assertThat(ids).element(6).isEqualTo(waitingBookingForItem1.getId());
        assertThat(ids).element(7).isEqualTo(waitingBookingForItem2.getId());
        assertThat(ids).element(8).isEqualTo(rejectedBookingForItem1.getId());
        assertThat(ids).element(9).isEqualTo(rejectedBookingForItem2.getId());
        assertThat(item1.equals(item2)).isFalse();
    }

    @Test
    public void getAllBookingsForItemsUser() {
        initializationItem2AndBookings();
        userRepository.save(user1);
        userRepository.save(user2);
        itemRepository.save(item1);
        itemRepository.save(item2);
        addBookingsInDb();
        var findBookingList = bookingService
                .getAllBookingsForItemsUser(PageRequest.of(0, 10), user1.getId(), "ALL");
        //then
        assertThat(findBookingList.getBookings().size()).isEqualTo(5);
        List<Long> ids = findBookingList.getBookings().stream().map(BookingDtoResponse::getId).sorted().collect(Collectors.toList());
        assertThat(ids).first().isEqualTo(pastBookingForItem1.getId());
        assertThat(ids).element(1).isEqualTo(currentBookingForItem1.getId());
        assertThat(ids).element(2).isEqualTo(futureBookingForItem1.getId());
        assertThat(ids).element(3).isEqualTo(waitingBookingForItem1.getId());
        assertThat(ids).element(4).isEqualTo(rejectedBookingForItem1.getId());
    }

    @Test
    public void getAllBookingsForUserWhenStateIsCurrent() {
        initializationItem2AndBookings();
        userRepository.save(user1);
        userRepository.save(user2);
        itemRepository.save(item1);
        itemRepository.save(item2);
        addBookingsInDb();
        var findBookingList = bookingService
                .getAllBookingsForUser(PageRequest.of(0, 10), user2.getId(), "CURRENT");
        assertThat(findBookingList.getBookings().size()).isEqualTo(2);
        List<Long> ids = findBookingList.getBookings().stream().map(BookingDtoResponse::getId).sorted().collect(Collectors.toList());
        assertThat(ids).last().isEqualTo(currentBookingForItem2.getId());
        assertThat(ids).first().isEqualTo(currentBookingForItem1.getId());
    }

    @Test
    public void getAllBookingsForItemsUserWhenStateIsCurrent() {
        //given
        initializationItem2AndBookings();
        userRepository.save(user1);
        userRepository.save(user2);
        itemRepository.save(item1);
        itemRepository.save(item2);
        addBookingsInDb();
        var findBookingList = bookingService
                .getAllBookingsForItemsUser(PageRequest.of(0, 10), user1.getId(), "CURRENT");

        List<Long> ids = findBookingList.getBookings().stream().map(BookingDtoResponse::getId).collect(Collectors.toList());
        assertThat(ids).singleElement().isEqualTo(currentBookingForItem1.getId());
    }

    @Test
    public void getAllBookingsForUserWhenStateIsPast() {
        initializationItem2AndBookings();
        userRepository.save(user1);
        userRepository.save(user2);
        itemRepository.save(item1);
        itemRepository.save(item2);
        addBookingsInDb();
        var findBookingList = bookingService
                .getAllBookingsForUser(PageRequest.of(0, 10), user2.getId(), "PAST");
        assertThat(findBookingList.getBookings().size()).isEqualTo(2); //6
        List<Long> ids = findBookingList.getBookings().stream().map(BookingDtoResponse::getId).sorted().collect(Collectors.toList());
        assertThat(ids).first().isEqualTo(pastBookingForItem1.getId());
        assertThat(ids).last().isEqualTo(pastBookingForItem2.getId());
    }

    @Test
    public void getAllBookingsForItemsUserWhenStateIsPast() {
        initializationItem2AndBookings();
        userRepository.save(user1);
        userRepository.save(user2);
        itemRepository.save(item1);
        itemRepository.save(item2);
        addBookingsInDb();
        var findBookingList = bookingService
                .getAllBookingsForItemsUser(PageRequest.of(0, 10), user1.getId(), "PAST");
        List<Long> ids = findBookingList.getBookings().stream().map(BookingDtoResponse::getId).collect(Collectors.toList());
        assertThat(ids).singleElement().isEqualTo(pastBookingForItem1.getId());
    }

    @Test
    public void getAllBookingsForUserWhenStateIsFuture() {
        initializationItem2AndBookings();
        userRepository.save(user1);
        userRepository.save(user2);
        itemRepository.save(item1);
        itemRepository.save(item2);
        addBookingsInDb();
        var findBookingList = bookingService
                .getAllBookingsForUser(PageRequest.of(0, 10), user2.getId(), "Future");
        assertThat(findBookingList.getBookings().size()).isEqualTo(6); //2
        List<Long> ids = findBookingList.getBookings().stream().map(BookingDtoResponse::getId).sorted().collect(Collectors.toList());
        assertThat(ids).first().isEqualTo(futureBookingForItem1.getId());
        assertThat(ids).element(1).isEqualTo(futureBookingForItem2.getId());
        assertThat(ids).element(2).isEqualTo(waitingBookingForItem1.getId());
        assertThat(ids).element(3).isEqualTo(waitingBookingForItem2.getId());
        assertThat(ids).element(4).isEqualTo(rejectedBookingForItem1.getId());
        assertThat(ids).element(5).isEqualTo(rejectedBookingForItem2.getId());
    }

    @Test
    public void getAllBookingsForItemsUserWhenStateIsFuture() {
        initializationItem2AndBookings();
        userRepository.save(user1);
        userRepository.save(user2);
        itemRepository.save(item1);
        itemRepository.save(item2);
        addBookingsInDb();

        var findBookingList = bookingService
                .getAllBookingsForItemsUser(PageRequest.of(0, 10), user1.getId(), "Future");
        List<Long> ids = findBookingList.getBookings().stream().map(BookingDtoResponse::getId).sorted().collect(Collectors.toList());
        assertThat(findBookingList.getBookings().size()).isEqualTo(3);//1
        assertThat(ids).first().isEqualTo(futureBookingForItem1.getId());
        assertThat(ids).element(1).isEqualTo(waitingBookingForItem1.getId());
        assertThat(ids).element(2).isEqualTo(rejectedBookingForItem1.getId());
    }

    @Test
    public void getAllBookingsForUserWhenStateIsWaiting() {
        initializationItem2AndBookings();
        userRepository.save(user1);
        userRepository.save(user2);
        itemRepository.save(item1);
        itemRepository.save(item2);
        addBookingsInDb();
        var findBookingList = bookingService
                .getAllBookingsForUser(PageRequest.of(0, 10), user2.getId(), "waiting");
        List<Long> ids = findBookingList.getBookings().stream().map(BookingDtoResponse::getId).sorted().collect(Collectors.toList());
        assertThat(findBookingList.getBookings().size()).isEqualTo(2);
        assertThat(ids).first().isEqualTo(waitingBookingForItem1.getId());
        assertThat(ids).last().isEqualTo(waitingBookingForItem2.getId());
    }

    @Test
    public void getAllBookingsForItemsUserWhenStateIsWaiting() {
        initializationItem2AndBookings();
        userRepository.save(user1);
        userRepository.save(user2);
        itemRepository.save(item1);
        itemRepository.save(item2);
        addBookingsInDb();
        var findBookingList = bookingService
                .getAllBookingsForItemsUser(PageRequest.of(0, 10), user1.getId(), "waiting");

        List<Long> ids = findBookingList.getBookings().stream().map(BookingDtoResponse::getId).collect(Collectors.toList());
        assertThat(ids).singleElement().isEqualTo(waitingBookingForItem1.getId());
    }

    @Test
    public void getAllBookingsForUserWhenStateIsRejected() {
        initializationItem2AndBookings();
        userRepository.save(user1);
        userRepository.save(user2);
        itemRepository.save(item1);
        itemRepository.save(item2);
        addBookingsInDb();
        var findBookingList = bookingService
                .getAllBookingsForUser(PageRequest.of(0, 10), user2.getId(), "rejected");
        List<Long> ids = findBookingList.getBookings().stream().map(BookingDtoResponse::getId).sorted().collect(Collectors.toList());
        assertThat(findBookingList.getBookings().size()).isEqualTo(2);
        assertThat(ids).first().isEqualTo(rejectedBookingForItem1.getId());
        assertThat(ids).last().isEqualTo(rejectedBookingForItem2.getId());
    }

    @Test
    public void getAllBookingsForItemsUserWhenStateIsRejected() {
        //given
        initializationItem2AndBookings();
        userRepository.save(user1);
        userRepository.save(user2);
        itemRepository.save(item1);
        itemRepository.save(item2);
        addBookingsInDb();
        var findBookingList = bookingService
                .getAllBookingsForItemsUser(PageRequest.of(0, 10), user1.getId(), "rejected");
        List<Long> ids = findBookingList.getBookings().stream().map(BookingDtoResponse::getId).collect(Collectors.toList());
        assertThat(ids).singleElement().isEqualTo(rejectedBookingForItem1.getId());
    }

    @Test
    public void getBookingListWithUnknownState() {
        userRepository.save(user1);
        assertThatThrownBy(
                () -> bookingService.getAllBookingsForUser(PageRequest.of(0, 10), user1.getId(), "qwe")
        ).isInstanceOf(StateException.class);
    }

    @Test
    public void getAllBookingsForUserWhenUserNotFound() {
        userRepository.save(user1);
        assertThatThrownBy(
                () -> bookingService.getAllBookingsForUser(PageRequest.of(0, 10), 50L, "ALL")
        ).isInstanceOf(ObjectNotFoundException.class);
    }

    @Test
    public void getAllBookingsForItemsUserWhenUserNotFound() {
        initializationItem2AndBookings();
        userRepository.save(user1);
        userRepository.save(user2);
        itemRepository.save(item1);
        itemRepository.save(item2);
        addBookingsInDb();
        assertThatThrownBy(
                () -> bookingService.getAllBookingsForItemsUser(PageRequest.of(0, 10), 50L, "ALL")
        ).isInstanceOf(RuntimeException.class);
    }

    @Test
    public void getAllBookingsForItemsUserWhenUserNotExistingBooking() {
        userRepository.save(user1);
        assertThatThrownBy(
                () -> bookingService.getAllBookingsForItemsUser(PageRequest.of(0, 10), user1.getId(), "ALL")
        ).isInstanceOf(RuntimeException.class);
    }

    @SneakyThrows()
    private void initializationItem2AndBookings() {
        currentBookingForItem1 = new Booking();
        currentBookingForItem1.setStart(LocalDateTime.now().minusDays(1));
        currentBookingForItem1.setEnd(LocalDateTime.now().plusDays(1));
        currentBookingForItem1.setItem(item1);
        currentBookingForItem1.setBooker(user2);
        currentBookingForItem1.setStatus(Status.APPROVED);

        currentBookingForItem2 = new Booking();
        currentBookingForItem2.setStart(LocalDateTime.now().minusDays(1));
        currentBookingForItem2.setEnd(LocalDateTime.now().plusDays(1));
        currentBookingForItem2.setItem(item2);
        currentBookingForItem2.setBooker(user2);
        currentBookingForItem2.setStatus(Status.APPROVED);

        pastBookingForItem1 = new Booking();
        pastBookingForItem1.setStart(LocalDateTime.now().minusDays(2));
        pastBookingForItem1.setEnd(LocalDateTime.now().minusDays(1));
        pastBookingForItem1.setItem(item1);
        pastBookingForItem1.setBooker(user2);
        pastBookingForItem1.setStatus(Status.APPROVED);

        pastBookingForItem2 = new Booking();
        pastBookingForItem2.setStart(LocalDateTime.now().minusDays(2));
        pastBookingForItem2.setEnd(LocalDateTime.now().minusDays(1));
        pastBookingForItem2.setItem(item2);
        pastBookingForItem2.setBooker(user2);
        pastBookingForItem2.setStatus(Status.APPROVED);

        futureBookingForItem1 = new Booking();
        futureBookingForItem1.setStart(LocalDateTime.now().plusDays(1));
        futureBookingForItem1.setEnd(LocalDateTime.now().plusDays(2));
        futureBookingForItem1.setItem(item1);
        futureBookingForItem1.setBooker(user2);
        futureBookingForItem1.setStatus(Status.APPROVED);

        futureBookingForItem2 = new Booking();
        futureBookingForItem2.setStart(LocalDateTime.now().plusDays(1));
        futureBookingForItem2.setEnd(LocalDateTime.now().plusDays(2));
        futureBookingForItem2.setItem(item2);
        futureBookingForItem2.setBooker(user2);
        futureBookingForItem2.setStatus(Status.APPROVED);

        waitingBookingForItem1 = new Booking();
        waitingBookingForItem1.setStart(LocalDateTime.now().plusHours(1));
        waitingBookingForItem1.setEnd(LocalDateTime.now().plusHours(2));
        waitingBookingForItem1.setItem(item1);
        waitingBookingForItem1.setBooker(user2);
        waitingBookingForItem1.setStatus(Status.WAITING);

        waitingBookingForItem2 = new Booking();
        waitingBookingForItem2.setStart(LocalDateTime.now().plusHours(1));
        waitingBookingForItem2.setEnd(LocalDateTime.now().plusHours(2));
        waitingBookingForItem2.setItem(item2);
        waitingBookingForItem2.setBooker(user2);
        waitingBookingForItem2.setStatus(Status.WAITING);

        rejectedBookingForItem1 = new Booking();
        rejectedBookingForItem1.setStart(LocalDateTime.now().plusHours(1));
        rejectedBookingForItem1.setEnd(LocalDateTime.now().plusHours(2));
        rejectedBookingForItem1.setItem(item1);
        rejectedBookingForItem1.setBooker(user2);
        rejectedBookingForItem1.setStatus(Status.REJECTED);

        rejectedBookingForItem2 = new Booking();
        rejectedBookingForItem2.setStart(LocalDateTime.now().plusHours(1));
        rejectedBookingForItem2.setEnd(LocalDateTime.now().plusHours(2));
        rejectedBookingForItem2.setItem(item2);
        rejectedBookingForItem2.setBooker(user2);
        rejectedBookingForItem2.setStatus(Status.REJECTED);
    }

    @SneakyThrows
    private void addBookingsInDb() {
        bookingRepository.save(pastBookingForItem1);
        bookingRepository.save(pastBookingForItem2);
        bookingRepository.save(currentBookingForItem1);
        bookingRepository.save(currentBookingForItem2);
        bookingRepository.save(futureBookingForItem1);
        bookingRepository.save(futureBookingForItem2);
        bookingRepository.save(waitingBookingForItem1);
        bookingRepository.save(waitingBookingForItem2);
        bookingRepository.save(rejectedBookingForItem1);
        bookingRepository.save(rejectedBookingForItem2);
    }
}
