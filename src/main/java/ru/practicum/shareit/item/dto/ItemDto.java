package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.booking.dto.BookingDtoShort;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ItemDto {
    private long id;

    @NotBlank(message = "Поле с именем не должно быть пустым.")
    private String name;

    @NotBlank(message = "Поле с описанием не должно быть пустым.")
    private String description;

    @NotNull(message = "Поле Available не должно быть пустым.")
    private Boolean available;
    private BookingDtoShort lastBooking;
    private BookingDtoShort nextBooking;
    private List<CommentData> comments;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CommentData {
        private long id;
        private String text;
        private String authorName;
        private LocalDateTime created;
    }

}