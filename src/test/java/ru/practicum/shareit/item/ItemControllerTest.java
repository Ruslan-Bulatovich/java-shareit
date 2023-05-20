package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.service.ItemService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ItemControllerTest {
    private final ObjectMapper objectMapper;
    @MockBean
    private ItemService itemService;
    private final MockMvc mvc;
    private ItemDto item1;
    private ItemDtoResponse itemDtoResponse;
    private ItemDtoUpdate itemDtoUpdate;
    private final String userIdHeader = "X-Sharer-User-Id";

    @BeforeEach
    public void setUp() {
        item1 = ItemDto.builder()
                .name("item test")
                .description("item test description")
                .available(Boolean.TRUE)
                .build();
        itemDtoResponse = ItemDtoResponse.builder()
                .id(1L)
                .name(item1.getName())
                .description(item1.getDescription())
                .available(Boolean.TRUE)
                .build();
        itemDtoUpdate = ItemDtoUpdate.builder()
                .name("update item test")
                .description("update test description")
                .build();
    }

    @Test
    public void createItem() throws Exception {
        //when
        when(itemService.createItem(any(ItemDto.class), anyLong())).thenReturn(itemDtoResponse);
        //when
        mvc.perform(
                        post("/items")
                                .header(userIdHeader, 1)
                                .content(objectMapper.writeValueAsString(item1))
                                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                //then
                .andExpectAll(
                        status().isCreated(),
                        content().json(objectMapper.writeValueAsString(itemDtoResponse))
                );
    }

    @SneakyThrows
    @Test
    public void createItemWithIncorrectUserId() {
        //when
        mvc.perform(
                        post("/items")
                                .header(userIdHeader, 0)
                                .content(objectMapper.writeValueAsString(item1))
                                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                //then
                .andExpectAll(
                        status().isBadRequest()
                );
        verify(itemService, times(0)).createItem(any(ItemDto.class), anyLong());
    }

    @SneakyThrows
    @Test
    public void createItemWithIncorrectName() {
        //given
        item1.setName("  test name");
        //when
        mvc.perform(
                        post("/items")
                                .header(userIdHeader, 1)
                                .content(objectMapper.writeValueAsString(item1))
                                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                //then
                .andExpectAll(
                        status().isBadRequest()
                );
        verify(itemService, times(0)).createItem(any(ItemDto.class), anyLong());
    }

    @SneakyThrows
    @Test
    public void createItemWithIncorrectDescription() {
        //given
        item1.setDescription("  test description");
        //when
        mvc.perform(
                        post("/items")
                                .header(userIdHeader, 1)
                                .content(objectMapper.writeValueAsString(item1))
                                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                //then
                .andExpectAll(
                        status().isBadRequest()
                );
        verify(itemService, times(0)).createItem(any(ItemDto.class), anyLong());
    }

    @SneakyThrows
    @Test
    public void createItemWithIncorrectAvailable() {
        //given
        item1.setAvailable(null);
        //when
        mvc.perform(
                        post("/items")
                                .header(userIdHeader, 1)
                                .content(objectMapper.writeValueAsString(item1))
                                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                //then
                .andExpectAll(
                        status().isBadRequest()
                );
        verify(itemService, times(0)).createItem(any(ItemDto.class), anyLong());
    }

    @SneakyThrows
    @Test
    public void createItemWithIncorrectIdRequest() {
        //given
        item1.setRequestId(0L);
        //when
        mvc.perform(
                        post("/items")
                                .header(userIdHeader, 1)
                                .content(objectMapper.writeValueAsString(item1))
                                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                //then
                .andExpectAll(
                        status().isBadRequest()
                );
        verify(itemService, times(0)).createItem(any(ItemDto.class), anyLong());
    }

    @SneakyThrows
    @Test
    public void updateItem() {
        //given
        itemDtoResponse.setName(itemDtoUpdate.getName());
        itemDtoResponse.setDescription(itemDtoUpdate.getDescription());
        //when
        when(itemService.updateItem(anyLong(), anyLong(), any(ItemDtoUpdate.class))).thenReturn(itemDtoResponse);
        mvc.perform(
                        patch("/items/1")
                                .header(userIdHeader, 1)
                                .content(objectMapper.writeValueAsString(item1))
                                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                //then
                .andExpectAll(
                        status().isOk(),
                        content().json(objectMapper.writeValueAsString(itemDtoResponse))
                );
    }

    @SneakyThrows
    @Test
    public void updateItemWithIncorrectUserId() {
        //when
        mvc.perform(
                        patch("/items/1")
                                .header(userIdHeader, 0)
                                .content(objectMapper.writeValueAsString(item1))
                                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                //then
                .andExpectAll(
                        status().isBadRequest()
                );
        verify(itemService, times(0)).updateItem(anyLong(), anyLong(), any(ItemDtoUpdate.class));
    }

    @SneakyThrows
    @Test
    public void updateItemWithIncorrectItemId() {
        //when
        mvc.perform(
                        patch("/items/0")
                                .header(userIdHeader, 1)
                                .content(objectMapper.writeValueAsString(item1))
                                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                //then
                .andExpectAll(
                        status().isBadRequest()
                );
        verify(itemService, times(0)).updateItem(anyLong(), anyLong(), any(ItemDtoUpdate.class));
    }

    @SneakyThrows
    @Test
    public void updateItemWithIncorrectName() {
        //given
        itemDtoUpdate.setName("    updated name");
        //when
        mvc.perform(
                        patch("/items/0")
                                .header(userIdHeader, 1)
                                .content(objectMapper.writeValueAsString(item1))
                                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                //then
                .andExpectAll(
                        status().isBadRequest()
                );
        verify(itemService, times(0)).updateItem(anyLong(), anyLong(), any(ItemDtoUpdate.class));
    }

    @SneakyThrows
    @Test
    public void updateItemWithIncorrectDescription() {
        //given
        itemDtoUpdate.setDescription("   updated description");
        //when
        mvc.perform(
                        patch("/items/0")
                                .header(userIdHeader, 1)
                                .content(objectMapper.writeValueAsString(item1))
                                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                //then
                .andExpectAll(
                        status().isBadRequest()
                );
        verify(itemService, times(0)).updateItem(anyLong(), anyLong(), any(ItemDtoUpdate.class));
    }

    @SneakyThrows
    @Test
    public void getItemById() {
        //when
        when(itemService.getItemByItemId(anyLong(), anyLong())).thenReturn(itemDtoResponse);
        mvc.perform(
                        get("/items/1")
                                .header(userIdHeader, 1))
                .andDo(print())
                //then
                .andExpectAll(
                        status().isOk(),
                        content().json(objectMapper.writeValueAsString(itemDtoResponse))
                );
    }

    @SneakyThrows
    @Test
    public void getItemByIdWithIncorrectUserId() {
        //when
        mvc.perform(
                        get("/items/1")
                                .header(userIdHeader, 0))
                .andDo(print())
                //then
                .andExpectAll(
                        status().isBadRequest()
                );
        verify(itemService, times(0)).getItemByItemId(anyLong(), anyLong());
    }

    @SneakyThrows
    @Test
    public void getItemByIncorrectId() {
        //when
        mvc.perform(
                        get("/items/0")
                                .header(userIdHeader, 1))
                .andDo(print())
                //then
                .andExpectAll(
                        status().isBadRequest()
                );
        verify(itemService, times(0)).getItemByItemId(anyLong(), anyLong());
    }

    @SneakyThrows
    @Test
    public void getPersonalItems() {
        //given
        var itemListDto = ItemListDto.builder().items(List.of(itemDtoResponse)).build();
        //when
        when(itemService.getPersonalItems(any(Pageable.class), anyLong())).thenReturn(itemListDto);
        mvc.perform(
                        get("/items")
                                .param("from", "0")
                                .param("size", "1")
                                .header("X-Sharer-User-Id", 1))
                .andExpectAll(
                        status().isOk(),
                        content().json(objectMapper.writeValueAsString(itemListDto))
                );
    }

    @SneakyThrows
    @Test
    public void getPersonalItemsWithIncorrectUserId() {
        //when
        mvc.perform(
                        get("/items")
                                .param("from", "0")
                                .param("size", "1")
                                .header(userIdHeader, 0))
                .andExpectAll(
                        status().isBadRequest()
                );
        verify(itemService, times(0)).getPersonalItems(any(Pageable.class), anyLong());
    }

    @SneakyThrows
    @Test
    public void getPersonalItemsWithIncorrectParamFrom() {
        //when
        mvc.perform(
                        get("/items")
                                .param("from", "-1")
                                .param("size", "1")
                                .header(userIdHeader, 1))
                .andExpectAll(
                        status().isBadRequest()
                );
        verify(itemService, times(0)).getPersonalItems(any(Pageable.class), anyLong());
    }

    @SneakyThrows
    @Test
    public void getPersonalItemsWithIncorrectParamSize() {
        //when
        mvc.perform(
                        get("/items")
                                .param("from", "0")
                                .param("size", "99999")
                                .header(userIdHeader, 1))
                .andExpectAll(
                        status().isBadRequest()
                );
        verify(itemService, times(0)).getPersonalItems(any(Pageable.class), anyLong());
    }

    @SneakyThrows
    @Test
    public void getFoundItems() {
        //given
        var itemListDto = ItemListDto.builder().items(List.of(itemDtoResponse)).build();
        //when
        when(itemService.getFoundItems(any(Pageable.class), anyString())).thenReturn(itemListDto);
        mvc.perform(
                        get("/items/search")
                                .param("from", "0")
                                .param("size", "1")
                                .param("text", "description"))
                .andExpectAll(
                        status().isOk(),
                        content().json(objectMapper.writeValueAsString(itemListDto))
                );
    }

    @SneakyThrows
    @Test
    public void getFoundItemsWitchIncorrectParamFrom() {
        //when
        mvc.perform(
                        get("/items/search")
                                .param("from", "-1")
                                .param("size", "1")
                                .param("text", "description"))
                .andExpectAll(
                        status().isBadRequest()
                );
        verify(itemService, times(0)).getFoundItems(any(Pageable.class), anyString());
    }

    @SneakyThrows
    @Test
    public void getFoundItemsWitchIncorrectParamSize() {
        //when
        mvc.perform(
                        get("/items/search")
                                .param("from", "0")
                                .param("size", "0")
                                .param("text", "description"))
                .andExpectAll(
                        status().isBadRequest()
                );
        verify(itemService, times(0)).getFoundItems(any(Pageable.class), anyString());
    }

    @SneakyThrows
    @Test
    public void addComment() {
        //given
        var comment = CommentDto.builder()
                .text("Nice!")
                .build();
        var commentDtoResponse = CommentDtoResponse.builder()
                .id(1L)
                .authorName(item1.getName())
                .text(comment.getText())
                .created(LocalDateTime.now())
                .build();
        //when
        when(itemService.addComment(anyLong(), anyLong(), any(CommentDto.class))).thenReturn(commentDtoResponse);
        mvc.perform(
                        post("/items/1/comment")
                                .header(userIdHeader, 1)
                                .content(objectMapper.writeValueAsString(comment))
                                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                //then
                .andExpectAll(
                        status().isOk(),
                        content().json(objectMapper.writeValueAsString(commentDtoResponse))
                );
    }

    @SneakyThrows
    @Test
    public void addCommentWithEmptyText() {
        //given
        var comment = CommentDto.builder()
                .text("     ")
                .build();
        //when
        mvc.perform(
                        post("/items/1/comment")
                                .header(userIdHeader, 1)
                                .content(objectMapper.writeValueAsString(comment))
                                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                //then
                .andExpectAll(
                        status().isBadRequest()
                );
        verify(itemService, times(0)).addComment(anyLong(), anyLong(), any(CommentDto.class));
    }

    @SneakyThrows
    @Test
    public void addCommentWithIncorrectItemId() {
        //given
        var comment = CommentDto.builder()
                .text("     ")
                .build();
        //when
        mvc.perform(
                        post("/items/0/comment")
                                .header(userIdHeader, 1)
                                .content(objectMapper.writeValueAsString(comment))
                                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                //then
                .andExpectAll(
                        status().isBadRequest()
                );
        verify(itemService, times(0)).addComment(anyLong(), anyLong(), any(CommentDto.class));
    }

    @SneakyThrows
    @Test
    public void addCommentWithIncorrectUserId() {
        //given
        var comment = CommentDto.builder()
                .text("     ")
                .build();
        //when
        mvc.perform(
                        post("/items/1/comment")
                                .header(userIdHeader, 0)
                                .content(objectMapper.writeValueAsString(comment))
                                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                //then
                .andExpectAll(
                        status().isBadRequest()
                );
        verify(itemService, times(0)).addComment(anyLong(), anyLong(), any(CommentDto.class));
    }
}
