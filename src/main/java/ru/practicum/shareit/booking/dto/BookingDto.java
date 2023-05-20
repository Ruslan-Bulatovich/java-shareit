package ru.practicum.shareit.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.booking.model.Status;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookingDto {
    private long id;
    private ItemData item;
    private UserData booker;
    private Status status;
    private LocalDateTime start;
    private LocalDateTime end;


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
