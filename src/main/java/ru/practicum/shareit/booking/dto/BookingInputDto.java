package ru.practicum.shareit.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookingInputDto {
    private long id;
    private long itemId;
    @FutureOrPresent(message = "Дата не должна быть в прошлом")
    @NotNull(message = "Дата не должна быть пустой")
    private LocalDateTime start;
    @FutureOrPresent(message = "Дата не должна быть в прошлом")
    @NotNull(message = "Дата не должна быть пустой")
    private LocalDateTime end;
}
