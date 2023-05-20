package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoResponse;
import ru.practicum.shareit.booking.enums.Status;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.error.handler.exception.StateException;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.model.User;


import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase
@ActiveProfiles("test")
@Sql(scripts = {"file:src/main/resources/schema.sql"})
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BookingServiceTest extends Bookings {
    private final BookingService bookingService;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private User user1;
    private User user2;
    private Item item1;
    private Item item2;
    private BookingDto booking1Dto;


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
        //given
        userRepository.save(user1);
        userRepository.save(user2);
        itemRepository.save(item1);
        //when
        var savedBooking = bookingService.createBooking(user2.getId(), booking1Dto);
        var findBooking = bookingService
                .getBookingByIdForOwnerAndBooker(savedBooking.getId(), user2.getId());
        //then
        assertThat(savedBooking).usingRecursiveComparison().ignoringFields("start", "end")
                .isEqualTo(findBooking);
    }

    @Test
    public void createBookingWhenEndBeforeStart() {
        //given
        booking1Dto.setEnd(LocalDateTime.now().plusDays(1));
        booking1Dto.setStart(LocalDateTime.now().plusDays(2));
        userRepository.save(user1);
        userRepository.save(user2);
        itemRepository.save(item1);
        assertThatThrownBy(
                //when
                () -> bookingService.createBooking(user2.getId(), booking1Dto)
                //then
        ).isInstanceOf(ResponseStatusException.class);
    }

    @Test
    public void createBookingWithNotExistingItem() {
        //given
        booking1Dto.setItemId(2L);
        userRepository.save(user1);
        userRepository.save(user2);
        itemRepository.save(item1);
        assertThatThrownBy(
                //when
                () -> bookingService.createBooking(user2.getId(), booking1Dto)
                //then
        ).isInstanceOf(ResponseStatusException.class);
    }

    @Test
    public void createBookingWhenBookerIsOwner() {
        //given
        userRepository.save(user1);
        userRepository.save(user2);
        itemRepository.save(item1);
        assertThatThrownBy(
                //when
                () -> bookingService.createBooking(user1.getId(), booking1Dto)
                //then
        ).isInstanceOf(ResponseStatusException.class);
    }

    @Test
    public void createBookingWhenNotExistingBooker() {
        userRepository.save(user1);
        userRepository.save(user2);
        itemRepository.save(item1);
        assertThatThrownBy(
                //when
                () -> bookingService.createBooking(99L, booking1Dto)
                //then
        ).isInstanceOf(ResponseStatusException.class);
    }

    @Test
    public void createBookingWithNotAvailableItem() {
        //given
        item1.setAvailable(Boolean.FALSE);
        userRepository.save(user1);
        userRepository.save(user2);
        itemRepository.save(item1);
        assertThatThrownBy(
                //when
                () -> bookingService.createBooking(user2.getId(), booking1Dto)
                //then
        ).isInstanceOf(ResponseStatusException.class);
    }

    @Test
    public void approveBooking() {
        //given
        userRepository.save(user1);
        userRepository.save(user2);
        itemRepository.save(item1);
        var savedBooking = bookingService.createBooking(user2.getId(), booking1Dto);
        //when
        var approvedBooking = bookingService
                .approveBooking(user1.getId(), savedBooking.getId(), "true");
        var findBooking = bookingService
                .getBookingByIdForOwnerAndBooker(savedBooking.getId(), user2.getId());
        //then
        assertThat(approvedBooking).usingRecursiveComparison().isEqualTo(findBooking);
    }

    @Test
    public void rejectBooking() {
        //given
        userRepository.save(user1);
        userRepository.save(user2);
        itemRepository.save(item1);
        var savedBooking = bookingService.createBooking(user2.getId(), booking1Dto);
        //when
        var approvedBooking = bookingService
                .approveBooking(user1.getId(), savedBooking.getId(), "FALSE");
        var findBooking = bookingService
                .getBookingByIdForOwnerAndBooker(savedBooking.getId(), user2.getId());
        //then
        assertThat(approvedBooking).usingRecursiveComparison().isEqualTo(findBooking);
    }

    @Test
    public void approveBookingWithIncorrectParamApproved() {
        //given
        userRepository.save(user1);
        userRepository.save(user2);
        itemRepository.save(item1);
        var savedBooking = bookingService.createBooking(user2.getId(), booking1Dto);
        assertThatThrownBy(
                //when
                () -> bookingService.approveBooking(user1.getId(), savedBooking.getId(), "truee")
                //then
        ).isInstanceOf(ResponseStatusException.class);
    }

    @Test
    public void approveBookingWithNotExistingBooking() {
        //given
        userRepository.save(user1);
        userRepository.save(user2);
        itemRepository.save(item1);
        bookingService.createBooking(user2.getId(), booking1Dto);
        assertThatThrownBy(
                //when
                () -> bookingService.approveBooking(user1.getId(), 99L, "true")
                //then
        ).isInstanceOf(ResponseStatusException.class);
    }

    @Test
    public void approveBookingWhenBookingIsNotWaiting() {
        //given
        userRepository.save(user1);
        userRepository.save(user2);
        itemRepository.save(item1);
        var savedBooking = bookingService.createBooking(user2.getId(), booking1Dto);
        bookingService.approveBooking(user1.getId(), savedBooking.getId(), "false");
        assertThatThrownBy(
                //when
                () -> bookingService.approveBooking(user1.getId(), savedBooking.getId(), "true")
                //then
        ).isInstanceOf(ResponseStatusException.class);
    }

    @Test
    public void approveBookingWhenUserIsNotOwner() {
        //given
        userRepository.save(user1);
        userRepository.save(user2);
        itemRepository.save(item1);
        var savedBooking = bookingService.createBooking(user2.getId(), booking1Dto);
        assertThatThrownBy(
                //when
                () -> bookingService.approveBooking(user2.getId(), savedBooking.getId(), "true")
                //then
        ).isInstanceOf(ResponseStatusException.class);
    }

    @Test
    public void getBookingWhenBookingNotFound() {
        //given
        userRepository.save(user1);
        userRepository.save(user2);
        itemRepository.save(item1);
        bookingService.createBooking(user2.getId(), booking1Dto);
        assertThatThrownBy(
                //when
                () -> bookingService.getBookingByIdForOwnerAndBooker(99L, user2.getId())
                //then
        ).isInstanceOf(ResponseStatusException.class);
    }

    @Test
    public void getBookingWhenUserIsNotOwnerOrBooker() {
        //given
        userRepository.save(user1);
        userRepository.save(user2);
        itemRepository.save(item1);
        var savedBooking = bookingService.createBooking(user2.getId(), booking1Dto);
        assertThatThrownBy(
                //when
                () -> bookingService.getBookingByIdForOwnerAndBooker(savedBooking.getId(), 10L)
                //then
        ).isInstanceOf(ResponseStatusException.class);
    }

    @Test
    public void getAllBookingForUserWhenStateIsAll() {
        //given
        initializationItem2AndBookings();
        userRepository.save(user1);
        userRepository.save(user2);
        itemRepository.save(item1);
        itemRepository.save(item2);
        addBookingsInDb();
        //when
        var findBookingList = bookingService
                .getAllBookingsForUser(PageRequest.of(0, 10), user2.getId(), "ALL");
        //then
        assertThat(findBookingList.getBookings().size()).isEqualTo(10);
        List<Long> ids = findBookingList.getBookings().stream().map(BookingDtoResponse::getId).collect(Collectors.toList());
        assertThat(ids).first().isEqualTo(futureBookingForItem2.getId());
        assertThat(ids).element(1).isEqualTo(futureBookingForItem1.getId());
        assertThat(ids).element(2).isEqualTo(rejectedBookingForItem2.getId());
        assertThat(ids).element(3).isEqualTo(rejectedBookingForItem1.getId());
        assertThat(ids).element(4).isEqualTo(waitingBookingForItem2.getId());
        assertThat(ids).element(5).isEqualTo(waitingBookingForItem1.getId());
        assertThat(ids).element(6).isEqualTo(currentBookingForItem2.getId());
        assertThat(ids).element(7).isEqualTo(currentBookingForItem1.getId());
        assertThat(ids).element(9).isEqualTo(pastBookingForItem1.getId());
        assertThat(ids).element(8).isEqualTo(pastBookingForItem2.getId());
        assertThat(item1.equals(item2)).isFalse();
    }

    @Test
    public void getAllBookingsForItemsUser() {
        //given
        initializationItem2AndBookings();
        userRepository.save(user1);
        userRepository.save(user2);
        itemRepository.save(item1);
        itemRepository.save(item2);
        addBookingsInDb();
        //when
        var findBookingList = bookingService
                .getAllBookingsForItemsUser(PageRequest.of(0, 10), user1.getId(), "ALL");
        //then
        assertThat(findBookingList.getBookings().size()).isEqualTo(5);
        List<Long> ids = findBookingList.getBookings().stream().map(BookingDtoResponse::getId)
                .collect(Collectors.toList());
        assertThat(ids).first().isEqualTo(futureBookingForItem1.getId());
        assertThat(ids).element(1).isEqualTo(rejectedBookingForItem1.getId());
        assertThat(ids).element(2).isEqualTo(waitingBookingForItem1.getId());
        assertThat(ids).element(3).isEqualTo(currentBookingForItem1.getId());
        assertThat(ids).element(4).isEqualTo(pastBookingForItem1.getId());
    }

    @Test
    public void getAllBookingsForUserWhenStateIsCurrent() {
        //given
        initializationItem2AndBookings();
        userRepository.save(user1);
        userRepository.save(user2);
        itemRepository.save(item1);
        itemRepository.save(item2);
        addBookingsInDb();
        //when
        var findBookingList = bookingService
                .getAllBookingsForUser(PageRequest.of(0, 10), user2.getId(), "CURRENT");
        //then
        assertThat(findBookingList.getBookings().size()).isEqualTo(2);
        List<Long> ids = findBookingList.getBookings().stream().map(BookingDtoResponse::getId).collect(Collectors.toList());
        assertThat(ids).first().isEqualTo(currentBookingForItem2.getId());
        assertThat(ids).last().isEqualTo(currentBookingForItem1.getId());
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
        //when
        var findBookingList = bookingService
                .getAllBookingsForItemsUser(PageRequest.of(0, 10), user1.getId(), "CURRENT");
        //then
        List<Long> ids = findBookingList.getBookings().stream().map(BookingDtoResponse::getId).collect(Collectors.toList());
        assertThat(ids).singleElement().isEqualTo(currentBookingForItem1.getId());
    }

    @Test
    public void getAllBookingsForUserWhenStateIsPast() {
        //given
        initializationItem2AndBookings();
        userRepository.save(user1);
        userRepository.save(user2);
        itemRepository.save(item1);
        itemRepository.save(item2);
        addBookingsInDb();
        //when
        var findBookingList = bookingService
                .getAllBookingsForUser(PageRequest.of(0, 10), user2.getId(), "PAST");
        //then
        assertThat(findBookingList.getBookings().size()).isEqualTo(2);
        List<Long> ids = findBookingList.getBookings().stream().map(BookingDtoResponse::getId).collect(Collectors.toList());
        assertThat(ids).first().isEqualTo(pastBookingForItem2.getId());
        assertThat(ids).last().isEqualTo(pastBookingForItem1.getId());
    }

    @Test
    public void getAllBookingsForItemsUserWhenStateIsPast() {
        //given
        initializationItem2AndBookings();
        userRepository.save(user1);
        userRepository.save(user2);
        itemRepository.save(item1);
        itemRepository.save(item2);
        addBookingsInDb();
        //when
        var findBookingList = bookingService
                .getAllBookingsForItemsUser(PageRequest.of(0, 10), user1.getId(), "PAST");
        //then
        List<Long> ids = findBookingList.getBookings().stream().map(BookingDtoResponse::getId).collect(Collectors.toList());
        assertThat(ids).singleElement().isEqualTo(pastBookingForItem1.getId());
    }

    @Test
    public void getAllBookingsForUserWhenStateIsFuture() {
        //given
        initializationItem2AndBookings();
        userRepository.save(user1);
        userRepository.save(user2);
        itemRepository.save(item1);
        itemRepository.save(item2);
        addBookingsInDb();
        //when
        var findBookingList = bookingService
                .getAllBookingsForUser(PageRequest.of(0, 10), user2.getId(), "Future");
        //then
        assertThat(findBookingList.getBookings().size()).isEqualTo(6);
        List<Long> ids = findBookingList.getBookings().stream().map(BookingDtoResponse::getId).collect(Collectors.toList());
        assertThat(ids).first().isEqualTo(futureBookingForItem2.getId());
        assertThat(ids).element(1).isEqualTo(futureBookingForItem1.getId());
        assertThat(ids).element(2).isEqualTo(rejectedBookingForItem2.getId());
        assertThat(ids).element(3).isEqualTo(rejectedBookingForItem1.getId());
        assertThat(ids).element(4).isEqualTo(waitingBookingForItem2.getId());
        assertThat(ids).element(5).isEqualTo(waitingBookingForItem1.getId());
    }

    @Test
    public void getAllBookingsForItemsUserWhenStateIsFuture() {
        //given
        initializationItem2AndBookings();
        userRepository.save(user1);
        userRepository.save(user2);
        itemRepository.save(item1);
        itemRepository.save(item2);
        addBookingsInDb();
        //when
        var findBookingList = bookingService
                .getAllBookingsForItemsUser(PageRequest.of(0, 10), user1.getId(), "Future");
        //then
        assertThat(findBookingList.getBookings().size()).isEqualTo(3);
        List<Long> ids = findBookingList.getBookings().stream().map(BookingDtoResponse::getId).collect(Collectors.toList());
        assertThat(ids).first().isEqualTo(futureBookingForItem1.getId());
        assertThat(ids).element(1).isEqualTo(rejectedBookingForItem1.getId());
        assertThat(ids).element(2).isEqualTo(waitingBookingForItem1.getId());
    }

    @Test
    public void getAllBookingsForUserWhenStateIsWaiting() {
        //given
        initializationItem2AndBookings();
        userRepository.save(user1);
        userRepository.save(user2);
        itemRepository.save(item1);
        itemRepository.save(item2);
        addBookingsInDb();
        //when
        var findBookingList = bookingService
                .getAllBookingsForUser(PageRequest.of(0, 10), user2.getId(), "waiting");
        //then
        assertThat(findBookingList.getBookings().size()).isEqualTo(2);
        List<Long> ids = findBookingList.getBookings().stream().map(BookingDtoResponse::getId).collect(Collectors.toList());
        assertThat(ids).first().isEqualTo(waitingBookingForItem2.getId());
        assertThat(ids).last().isEqualTo(waitingBookingForItem1.getId());
    }

    @Test
    public void getAllBookingsForItemsUserWhenStateIsWaiting() {
        //given
        initializationItem2AndBookings();
        userRepository.save(user1);
        userRepository.save(user2);
        itemRepository.save(item1);
        itemRepository.save(item2);
        addBookingsInDb();
        //when
        var findBookingList = bookingService
                .getAllBookingsForItemsUser(PageRequest.of(0, 10), user1.getId(), "waiting");
        //then
        List<Long> ids = findBookingList.getBookings().stream().map(BookingDtoResponse::getId).collect(Collectors.toList());
        assertThat(ids).singleElement().isEqualTo(waitingBookingForItem1.getId());
    }

    @Test
    public void getAllBookingsForUserWhenStateIsRejected() {
        //given
        initializationItem2AndBookings();
        userRepository.save(user1);
        userRepository.save(user2);
        itemRepository.save(item1);
        itemRepository.save(item2);
        addBookingsInDb();
        //when
        var findBookingList = bookingService
                .getAllBookingsForUser(PageRequest.of(0, 10), user2.getId(), "rejected");
        //then
        assertThat(findBookingList.getBookings().size()).isEqualTo(2);
        List<Long> ids = findBookingList.getBookings().stream().map(BookingDtoResponse::getId).collect(Collectors.toList());
        assertThat(ids).first().isEqualTo(rejectedBookingForItem2.getId());
        assertThat(ids).last().isEqualTo(rejectedBookingForItem1.getId());
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
        //when
        var findBookingList = bookingService
                .getAllBookingsForItemsUser(PageRequest.of(0, 10), user1.getId(), "rejected");
        //then
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
        ).isInstanceOf(RuntimeException.class);
    }

    @Test
    public void getAllBookingsForItemsUserWhenUserNotFound() {
        //given
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
        //given
        userRepository.save(user1);
        assertThatThrownBy(
                () -> bookingService.getAllBookingsForItemsUser(PageRequest.of(0, 10), user1.getId(), "ALL")
        ).isInstanceOf(RuntimeException.class);
    }

    @SneakyThrows
    private void initializationItem2AndBookings() {

        currentBookingForItem1 = new Booking();
        currentBookingForItem1.setStart(LocalDateTime.now().minusDays(1));
        currentBookingForItem1.setEnd(LocalDateTime.now().plusDays(1));
        currentBookingForItem1.setItem(item1);
        currentBookingForItem1.setBooker(user2);
        currentBookingForItem1.setStatus(Status.APPROVED);

        Thread.sleep(50);

        currentBookingForItem2 = new Booking();
        currentBookingForItem2.setStart(LocalDateTime.now().minusDays(1));
        currentBookingForItem2.setEnd(LocalDateTime.now().plusDays(1));
        currentBookingForItem2.setItem(item2);
        currentBookingForItem2.setBooker(user2);
        currentBookingForItem2.setStatus(Status.APPROVED);

        Thread.sleep(50);

        pastBookingForItem1 = new Booking();
        pastBookingForItem1.setStart(LocalDateTime.now().minusDays(2));
        pastBookingForItem1.setEnd(LocalDateTime.now().minusDays(1));
        pastBookingForItem1.setItem(item1);
        pastBookingForItem1.setBooker(user2);
        pastBookingForItem1.setStatus(Status.APPROVED);

        Thread.sleep(50);

        pastBookingForItem2 = new Booking();
        pastBookingForItem2.setStart(LocalDateTime.now().minusDays(2));
        pastBookingForItem2.setEnd(LocalDateTime.now().minusDays(1));
        pastBookingForItem2.setItem(item2);
        pastBookingForItem2.setBooker(user2);
        pastBookingForItem2.setStatus(Status.APPROVED);

        Thread.sleep(50);

        futureBookingForItem1 = new Booking();
        futureBookingForItem1.setStart(LocalDateTime.now().plusDays(1));
        futureBookingForItem1.setEnd(LocalDateTime.now().plusDays(2));
        futureBookingForItem1.setItem(item1);
        futureBookingForItem1.setBooker(user2);
        futureBookingForItem1.setStatus(Status.APPROVED);

        Thread.sleep(50);

        futureBookingForItem2 = new Booking();
        futureBookingForItem2.setStart(LocalDateTime.now().plusDays(1));
        futureBookingForItem2.setEnd(LocalDateTime.now().plusDays(2));
        futureBookingForItem2.setItem(item2);
        futureBookingForItem2.setBooker(user2);
        futureBookingForItem2.setStatus(Status.APPROVED);

        Thread.sleep(50);

        waitingBookingForItem1 = new Booking();
        waitingBookingForItem1.setStart(LocalDateTime.now().plusHours(1));
        waitingBookingForItem1.setEnd(LocalDateTime.now().plusHours(2));
        waitingBookingForItem1.setItem(item1);
        waitingBookingForItem1.setBooker(user2);
        waitingBookingForItem1.setStatus(Status.WAITING);

        Thread.sleep(50);

        waitingBookingForItem2 = new Booking();
        waitingBookingForItem2.setStart(LocalDateTime.now().plusHours(1));
        waitingBookingForItem2.setEnd(LocalDateTime.now().plusHours(2));
        waitingBookingForItem2.setItem(item2);
        waitingBookingForItem2.setBooker(user2);
        waitingBookingForItem2.setStatus(Status.WAITING);

        Thread.sleep(50);

        rejectedBookingForItem1 = new Booking();
        rejectedBookingForItem1.setStart(LocalDateTime.now().plusHours(1));
        rejectedBookingForItem1.setEnd(LocalDateTime.now().plusHours(2));
        rejectedBookingForItem1.setItem(item1);
        rejectedBookingForItem1.setBooker(user2);
        rejectedBookingForItem1.setStatus(Status.REJECTED);

        Thread.sleep(50);

        rejectedBookingForItem2 = new Booking();
        rejectedBookingForItem2.setStart(LocalDateTime.now().plusHours(1));
        rejectedBookingForItem2.setEnd(LocalDateTime.now().plusHours(2));
        rejectedBookingForItem2.setItem(item2);
        rejectedBookingForItem2.setBooker(user2);
        rejectedBookingForItem2.setStatus(Status.REJECTED);
    }

    @SneakyThrows
    private void addBookingsInDb() {
        //Thread.sleep(300);
        bookingRepository.save(currentBookingForItem1);
        bookingRepository.save(currentBookingForItem2);
        bookingRepository.save(pastBookingForItem1);
        bookingRepository.save(pastBookingForItem2);
        bookingRepository.save(futureBookingForItem1);
        bookingRepository.save(futureBookingForItem2);
        bookingRepository.save(waitingBookingForItem1);
        bookingRepository.save(waitingBookingForItem2);
        bookingRepository.save(rejectedBookingForItem1);
        bookingRepository.save(rejectedBookingForItem2);
    }
}
