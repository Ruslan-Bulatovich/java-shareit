package ru.practicum.shareit.item.dto;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
@Builder
public class ItemDto {
    @Size(max = 255)
    @NotBlank
    @NotNull(message = "Поле name обязательно")
    private String name;
    @Size(max = 500)
    @NotNull(message = "Поле description обязательно")
    private String description;
    @NotNull(message = "Поле available обязательно")
    private Boolean available;
    @Min(1)
    private Long requestId;
}
