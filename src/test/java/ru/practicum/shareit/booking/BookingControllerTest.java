package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoResponse;
import ru.practicum.shareit.booking.dto.BookingListDto;
import ru.practicum.shareit.booking.enums.Status;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.error.handler.exception.StateException;
import ru.practicum.shareit.item.dto.ItemShortDto;
import ru.practicum.shareit.user.dto.UserShortDto;

import java.time.LocalDateTime;
import java.util.List;

@SpringBootTest
@AutoConfigureMockMvc
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BookingControllerTest {
    private final ObjectMapper objectMapper;
    private final MockMvc mvc;
    @MockBean
    private final BookingService bookingService;
    private static BookingDto bookingDto;
    private static ItemShortDto itemShortDto;
    private static UserShortDto userShortDto;
    private BookingListDto bookingListDto;
    private static BookingDtoResponse bookingDtoResponse;
    private final String userIdHeader = "X-Sharer-User-Id";

    @BeforeEach
    public void setUp() {
        bookingDto = BookingDto.builder()
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .itemId(1L)
                .build();
        itemShortDto = ItemShortDto.builder()
                .id(bookingDto.getItemId())
                .name("test item")
                .build();
        userShortDto = UserShortDto.builder()
                .id(1L)
                .name("test name")
                .build();
        bookingDtoResponse = BookingDtoResponse.builder()
                .id(1L)
                .start(bookingDto.getStart())
                .end(bookingDto.getEnd())
                .item(itemShortDto)
                .booker(userShortDto)
                .status(bookingDto.getStatus())
                .build();
    }

    @Test
    @SneakyThrows
    public void createBooking() {
        //when
        when(bookingService.createBooking(anyLong(), any(BookingDto.class))).thenReturn(bookingDtoResponse);
        mvc.perform(
                        post("/bookings")
                                .content(objectMapper.writeValueAsString(bookingDto))
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(userIdHeader, 1))
                .andDo(print())
                //then
                .andExpectAll(
                        status().isCreated(),
                        content().json(objectMapper.writeValueAsString(bookingDtoResponse))
                );
    }

    @Test
    @SneakyThrows
    public void createBookingWithIncorrectBookerId() {
        //when
        mvc.perform(
                        post("/bookings")
                                .content(objectMapper.writeValueAsString(bookingDto))
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(userIdHeader, 0))
                .andDo(print())
                //then
                .andExpectAll(
                        status().isBadRequest()
                );
        verify(bookingService, times(0)).createBooking(anyLong(), any(BookingDto.class));
    }

    @Test
    @SneakyThrows
    public void createBookingWithIncorrectStart() {
        //given
        bookingDto.setStart(LocalDateTime.now().minusDays(1));
        //when
        mvc.perform(
                        post("/bookings")
                                .content(objectMapper.writeValueAsString(bookingDto))
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(userIdHeader, 1))
                .andDo(print())
                //then
                .andExpectAll(
                        status().isBadRequest()
                );
        verify(bookingService, times(0)).createBooking(anyLong(), any(BookingDto.class));
        bookingDto.setStart(LocalDateTime.now().plusDays(1));
    }

    @Test
    @SneakyThrows
    public void createBookingWithIncorrectEnd() {
        //given
        bookingDto.setEnd(LocalDateTime.now().minusDays(1));
        //when
        mvc.perform(
                        post("/bookings")
                                .content(objectMapper.writeValueAsString(bookingDto))
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(userIdHeader, 1))
                .andDo(print())
                //then
                .andExpectAll(
                        status().isBadRequest()
                );
        verify(bookingService, times(0)).createBooking(anyLong(), any(BookingDto.class));
        bookingDto.setEnd(LocalDateTime.now().plusDays(2));
    }

    @Test
    @SneakyThrows
    public void createBookingWithIncorrectItemId() {
        //given
        bookingDto.setItemId(null);
        //when
        mvc.perform(
                        post("/bookings")
                                .content(objectMapper.writeValueAsString(bookingDto))
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(userIdHeader, 1))
                .andDo(print())
                //then
                .andExpectAll(
                        status().isBadRequest()
                );
        verify(bookingService, times(0)).createBooking(anyLong(), any(BookingDto.class));
        bookingDto.setItemId(1L);
    }

    @Test
    @SneakyThrows
    public void approveBooking() {
        //given
        bookingDtoResponse.setStatus(Status.APPROVED);
        //when
        when(bookingService.approveBooking(anyLong(), anyLong(), anyString())).thenReturn(bookingDtoResponse);
        mvc.perform(
                        (patch("/bookings/1"))
                                .header(userIdHeader, 1)
                                .param("approved", "true"))
                .andDo(print())
                //then
                .andExpectAll(
                        status().isOk(),
                        content().json(objectMapper.writeValueAsString(bookingDtoResponse))
                );
        bookingDtoResponse.setStatus(Status.WAITING);
    }

    @Test
    @SneakyThrows
    public void approveBookingWitchIncorrectUserId() {
        //when
        mvc.perform(
                        (patch("/bookings/1"))
                                .header(userIdHeader, 0)
                                .param("approved", "true"))
                .andDo(print())
                //then
                .andExpectAll(
                        status().isBadRequest()
                );
        verify(bookingService, times(0)).approveBooking(anyLong(), anyLong(), anyString());
    }

    @Test
    @SneakyThrows
    public void approveBookingWitchIncorrectBookingId() {
        //when
        mvc.perform(
                        (patch("/bookings/0"))
                                .header(userIdHeader, 1)
                                .param("approved", "true"))
                .andDo(print())
                //then
                .andExpectAll(
                        status().isBadRequest()
                );
        verify(bookingService, times(0)).approveBooking(anyLong(), anyLong(), anyString());
    }

    @Test
    @SneakyThrows
    public void getBookingByIdForOwnerAndBooker() {
        //when
        when(bookingService.getBookingByIdForOwnerAndBooker(anyLong(), anyLong())).thenReturn(bookingDtoResponse);
        mvc.perform(
                        get("/bookings/1")
                                .header(userIdHeader, 1))
                .andDo(print())
                //then
                .andExpectAll(
                        status().isOk(),
                        content().json(objectMapper.writeValueAsString(bookingDtoResponse))
                );
    }

    @Test
    @SneakyThrows
    public void getBookingByIncorrectBookingIdForOwnerAndBooker() {
        //when
        mvc.perform(
                        get("/bookings/0")
                                .header(userIdHeader, 1))
                .andDo(print())
                .andExpectAll(
                        //then
                        status().isBadRequest()
                );
        verify(bookingService, times(0)).getBookingByIdForOwnerAndBooker(anyLong(), anyLong());
    }

    @Test
    @SneakyThrows
    public void getBookingByIdWithIncorrectUserIdForOwnerAndBooker() {
        //when
        mvc.perform(
                        get("/bookings/1"))
                .andDo(print())
                //then
                .andExpectAll(
                        status().isBadRequest()
                );
        verify(bookingService, times(0)).getBookingByIdForOwnerAndBooker(anyLong(), anyLong());
    }

    @Test
    @SneakyThrows
    public void getAllBookingsForUser() {
        //given
        bookingListDto = BookingListDto.builder()
                .bookings(List.of(bookingDtoResponse))
                .build();
        //when
        when(bookingService.getAllBookingsForUser(any(Pageable.class), anyLong(), anyString()))
                .thenReturn(bookingListDto);
        mvc.perform(
                        get("/bookings")
                                .header(userIdHeader, 1)
                                .param("from", "0")
                                .param("size", "2"))
                .andDo(print())
                //then
                .andExpectAll(
                        status().isOk(),
                        content().json(objectMapper.writeValueAsString(bookingListDto))
                );
    }

    @Test
    @SneakyThrows
    public void getAllBookingsForUserWithIncorrectState() {
        //given
        bookingListDto = BookingListDto.builder()
                .bookings(List.of(bookingDtoResponse))
                .build();
        //when
        when(bookingService.getAllBookingsForUser(any(Pageable.class), anyLong(), anyString()))
                .thenThrow(StateException.class);
        mvc.perform(
                        get("/bookings")
                                .header(userIdHeader, 1)
                                .param("from", "0")
                                .param("size", "2")
                                .param("state", "qwe"))
                .andDo(print())
                //then
                .andExpectAll(
                        status().isBadRequest()
                );
    }

    @Test
    @SneakyThrows
    public void getAllBookingsForUserWithIncorrectUserId() {
        //when
        mvc.perform(
                        get("/bookings")
                                .header(userIdHeader, 0)
                                .param("from", "0")
                                .param("size", "2"))
                .andDo(print())
                //then
                .andExpectAll(
                        status().isBadRequest()
                );
        verify(bookingService, times(0))
                .getAllBookingsForUser(any(Pageable.class), anyLong(), anyString());
    }

    @Test
    @SneakyThrows
    public void getAllBookingsForUserWithIncorrectParamFrom() {
        //when
        mvc.perform(
                        get("/bookings")
                                .header(userIdHeader, 1)
                                .param("from", "-1")
                                .param("size", "2"))
                .andDo(print())
                //then
                .andExpectAll(
                        status().isBadRequest()
                );
        verify(bookingService, times(0))
                .getAllBookingsForUser(any(Pageable.class), anyLong(), anyString());
    }

    @Test
    @SneakyThrows
    public void getAllBookingsForUserWithIncorrectParamSize() {
        //when
        mvc.perform(
                        get("/bookings")
                                .header(userIdHeader, 1)
                                .param("from", "0")
                                .param("size", "10000"))
                .andDo(print())
                //then
                .andExpectAll(
                        status().isBadRequest()
                );
        verify(bookingService, times(0))
                .getAllBookingsForUser(any(Pageable.class), anyLong(), anyString());
    }

    @Test
    @SneakyThrows
    public void getAllBookingsForItemsUser() {
        //given
        bookingListDto = BookingListDto.builder()
                .bookings(List.of(bookingDtoResponse))
                .build();
        //when
        when(bookingService.getAllBookingsForItemsUser(any(Pageable.class), anyLong(), anyString()))
                .thenReturn(bookingListDto);
        mvc.perform(
                        get("/bookings/owner")
                                .header(userIdHeader, 1)
                                .param("from", "0")
                                .param("size", "2"))
                .andDo(print())
                //then
                .andExpectAll(
                        status().isOk(),
                        content().json(objectMapper.writeValueAsString(bookingListDto))
                );
    }

    @Test
    @SneakyThrows
    public void getAllBookingsForItemsUserWithIncorrectUserId() {
        //when
        mvc.perform(
                        get("/bookings")
                                .header(userIdHeader, 0)
                                .param("from", "0")
                                .param("size", "2"))
                .andDo(print())
                //then
                .andExpectAll(
                        status().isBadRequest()
                );
        verify(bookingService, times(0))
                .getAllBookingsForItemsUser(any(Pageable.class), anyLong(), anyString());
    }

    @Test
    @SneakyThrows
    public void getAllBookingsForItemsUserWithIncorrectParamFrom() {
        //when
        mvc.perform(
                        get("/bookings/owner")
                                .header(userIdHeader, 1)
                                .param("from", "-1")
                                .param("size", "2"))
                .andDo(print())
                //then
                .andExpectAll(
                        status().isBadRequest()
                );
        verify(bookingService, times(0))
                .getAllBookingsForItemsUser(any(Pageable.class), anyLong(), anyString());
    }

    @Test
    @SneakyThrows
    public void getAllBookingsForItemsUserWithIncorrectParamSize() {
        //when
        mvc.perform(
                        get("/bookings/owner")
                                .header(userIdHeader, 1)
                                .param("from", "0")
                                .param("size", "10000"))
                .andDo(print())
                //then
                .andExpectAll(
                        status().isBadRequest()
                );
        verify(bookingService, times(0))
                .getAllBookingsForItemsUser(any(Pageable.class), anyLong(), anyString());
    }
}
