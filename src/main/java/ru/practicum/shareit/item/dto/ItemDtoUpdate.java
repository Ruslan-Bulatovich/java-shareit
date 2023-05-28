package ru.practicum.shareit.item.dto;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Builder
@Data
public class ItemDtoUpdate {
    @NotBlank
    @Size(max = 255)
    private String name;
    @NotBlank
    @Size(max = 500)
    private String description;
    private Boolean available;
}
