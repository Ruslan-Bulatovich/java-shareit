package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.request.dto.*;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ItemRequestControllerTest {
    private final ObjectMapper objectMapper;
    private final MockMvc mvc;
    @MockBean
    private final ItemRequestService itemRequestService;
    private ItemRequestDto itemRequestDto;
    private ItemRequestDtoResponse itemRequestDtoResponse;
    private ItemRequestListDto itemRequestListDto;
    private RequestDtoResponseWithMD requestDtoResponseWithMD;
    private ItemDataForRequestDto itemDataForRequestDto;
    private final String userIdHeader = "X-Sharer-User-Id";

    @BeforeEach
    public void setUp() {
        itemRequestDto = ItemRequestDto.builder()
                .description("test description")
                .build();
        itemRequestDtoResponse = ItemRequestDtoResponse.builder()
                .id(1L)
                .description(itemRequestDto.getDescription())
                .created(LocalDateTime.now())
                .build();
        requestDtoResponseWithMD = RequestDtoResponseWithMD.builder()
                .id(1L)
                .description(itemRequestDtoResponse.getDescription())
                .created(itemRequestDtoResponse.getCreated())
                .build();
        itemDataForRequestDto = ItemDataForRequestDto.builder()
                .id(1L)
                .name("test item name")
                .description("test description name")
                .requestId(1L)
                .available(Boolean.TRUE)
                .build();
    }

    @Test
    @SneakyThrows
    public void createRequest() {
        //when
        when(itemRequestService.createItemRequest(any(ItemRequestDto.class), anyLong()))
                .thenReturn(itemRequestDtoResponse);
        mvc.perform(
                        post("/requests")
                                .header(userIdHeader, 1)
                                .content(objectMapper.writeValueAsString(itemRequestDto))
                                .contentType(MediaType.APPLICATION_JSON)
                ).andDo(print())
                .andExpectAll(
                        //then
                        status().isOk(),
                        content().json(objectMapper.writeValueAsString(itemRequestDtoResponse))
                );
    }

    @Test
    @SneakyThrows
    public void createRequestWitchIncorrectUserId() {
        //when
        mvc.perform(
                        post("/requests")
                                .header(userIdHeader, 0)
                                .content(objectMapper.writeValueAsString(itemRequestDto))
                                .contentType(MediaType.APPLICATION_JSON)
                ).andDo(print())
                //then
                .andExpectAll(
                        status().isBadRequest()
                );
        verify(itemRequestService, times(0)).createItemRequest(any(ItemRequestDto.class), anyLong());
    }

    @Test
    @SneakyThrows
    public void createRequestWhenIncorrectDescription() {
        //given
        itemRequestDto.setDescription(" ");
        //when
        mvc.perform(
                        post("/requests")
                                .header(userIdHeader, 0)
                                .content(objectMapper.writeValueAsString(itemRequestDto))
                                .contentType(MediaType.APPLICATION_JSON)
                ).andDo(print())
                //then
                .andExpectAll(
                        status().isBadRequest()
                );
        verify(itemRequestService, times(0)).createItemRequest(any(ItemRequestDto.class), anyLong());
    }

    @Test
    @SneakyThrows
    public void getPrivateRequests() {
        //given
        requestDtoResponseWithMD.setItems(Set.of(itemDataForRequestDto));
        itemRequestListDto = ItemRequestListDto.builder()
                .requests(List.of(requestDtoResponseWithMD))
                .build();
        //when
        when(itemRequestService.getPrivateRequests(any(PageRequest.class), anyLong())).thenReturn(itemRequestListDto);
        mvc.perform(
                        get("/requests")
                                .header(userIdHeader, 1)
                                .param("from", "0")
                                .param("size", "2")
                ).andDo(print())
                .andExpectAll(
                        status().isOk(),
                        content().json(objectMapper.writeValueAsString(itemRequestListDto))
                );
    }

    @Test
    @SneakyThrows
    public void getPrivateRequestsWithIncorrectUserId() {
        //when
        mvc.perform(
                        get("/requests")
                                .header(userIdHeader, 0)
                                .param("from", "0")
                                .param("size", "2")
                ).andDo(print())
                .andExpectAll(
                        status().isBadRequest()
                );
        verify(itemRequestService, times(0)).getPrivateRequests(any(PageRequest.class), anyLong());
    }

    @Test
    @SneakyThrows
    public void getPrivateRequestsWithIncorrectParamFrom() {
        //when
        mvc.perform(
                        get("/requests")
                                .header(userIdHeader, 1)
                                .param("from", "-1")
                                .param("size", "2")
                ).andDo(print())
                .andExpectAll(
                        status().isBadRequest()
                );
        verify(itemRequestService, times(0)).getPrivateRequests(any(PageRequest.class), anyLong());
    }

    @Test
    @SneakyThrows
    public void getPrivateRequestsWithIncorrectParamSize() {
        //when
        mvc.perform(
                        get("/requests")
                                .header(userIdHeader, 1)
                                .param("from", "0")
                                .param("size", "-1")
                ).andDo(print())
                .andExpectAll(
                        status().isBadRequest()
                );
        verify(itemRequestService, times(0)).getPrivateRequests(any(PageRequest.class), anyLong());
    }

    @Test
    @SneakyThrows
    public void getOtherRequests() {
        //given
        requestDtoResponseWithMD.setItems(Set.of(itemDataForRequestDto));
        itemRequestListDto = ItemRequestListDto.builder()
                .requests(List.of(requestDtoResponseWithMD))
                .build();
        //when
        when(itemRequestService.getOtherRequests(any(PageRequest.class), anyLong())).thenReturn(itemRequestListDto);
        mvc.perform(
                        get("/requests/all")
                                .header(userIdHeader, 1)
                                .param("from", "0")
                                .param("size", "2")
                ).andDo(print())
                .andExpectAll(
                        status().isOk(),
                        content().json(objectMapper.writeValueAsString(itemRequestListDto))
                );
    }

    @Test
    @SneakyThrows
    public void getOtherRequestsWitchIncorrectUserId() {
        //when
        mvc.perform(
                        get("/requests/all")
                                .header(userIdHeader, 0)
                                .param("from", "0")
                                .param("size", "2")
                ).andDo(print())
                .andExpectAll(
                        //then
                        status().isBadRequest()
                );
        verify(itemRequestService, times(0)).getOtherRequests(any(PageRequest.class), anyLong());
    }

    @Test
    @SneakyThrows
    public void getOtherRequestsWitchIncorrectParamFrom() {
        //when
        mvc.perform(
                        get("/requests/all")
                                .header(userIdHeader, 1)
                                .param("from", "-1")
                                .param("size", "2")
                ).andDo(print())
                .andExpectAll(
                        //then
                        status().isBadRequest()
                );
        verify(itemRequestService, times(0)).getOtherRequests(any(PageRequest.class), anyLong());
    }

    @Test
    @SneakyThrows
    public void getOtherRequestsWitchIncorrectParamSize() {
        //when
        mvc.perform(
                        get("/requests/all")
                                .header(userIdHeader, 1)
                                .param("from", "0")
                                .param("size", "24343")
                ).andDo(print())
                .andExpectAll(
                        //then
                        status().isBadRequest()
                );
        verify(itemRequestService, times(0)).getOtherRequests(any(PageRequest.class), anyLong());
    }

    @Test
    @SneakyThrows
    public void getItemRequest() {
        //given
        requestDtoResponseWithMD.setItems(Set.of(itemDataForRequestDto));
        //when
        when(itemRequestService.getItemRequest(anyLong(), anyLong())).thenReturn(requestDtoResponseWithMD);
        mvc.perform(
                        get("/requests/1")
                                .header(userIdHeader, 1)
                ).andDo(print())
                .andExpectAll(
                        status().isOk(),
                        content().json(objectMapper.writeValueAsString(requestDtoResponseWithMD))
                );
    }

    @Test
    @SneakyThrows
    public void getItemRequestWitchIncorrectUserId() {
        //when
        mvc.perform(
                        get("/requests/1")
                                .header(userIdHeader, 0)
                ).andDo(print())
                .andExpectAll(
                        //then
                        status().isBadRequest()
                );
        verify(itemRequestService, times(0)).getItemRequest(anyLong(), anyLong());
    }

    @Test
    @SneakyThrows
    public void getItemRequestWitchIncorrectItemRequestId() {
        //when
        mvc.perform(
                        get("/requests/0")
                                .header(userIdHeader, 1)
                ).andDo(print())
                .andExpectAll(
                        //then
                        status().isBadRequest()
                );
        verify(itemRequestService, times(0)).getItemRequest(anyLong(), anyLong());
    }
}
