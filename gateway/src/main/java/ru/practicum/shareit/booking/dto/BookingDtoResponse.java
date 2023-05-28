package ru.practicum.shareit.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
public class BookingDtoResponse {
    private Long id;
    private LocalDateTime start;
    private LocalDateTime end;
    private ItemData item;
    private UserData booker;
    private Status status;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserData {
        private long id;
        private String email;
        private String name;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ItemData {
        private long id;
        private long userId;
        private String name;
        private String description;
        private Boolean available;
    }
}
