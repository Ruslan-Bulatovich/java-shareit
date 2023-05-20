package ru.practicum.shareit.item.dto;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Data
@Builder
public class ItemDto {
    @Pattern(regexp = "^[^ ].*[^ ]$", message = "Неккоректное имя")
    @Size(max = 255)
    @NotNull(message = "Поле name обязательно")
    private String name;
    @Pattern(regexp = "^[^ ].*[^ ]$", message = "Неккоректное описание")
    @Size(max = 500)
    @NotNull(message = "Поле description обязательно")
    private String description;
    @NotNull(message = "Поле available обязательно")
    private Boolean available;
    @Min(1)
    private Long requestId;
}
