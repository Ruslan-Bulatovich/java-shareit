package ru.practicum.shareit.user.dto;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
@Builder
public class UserDto {
    @NotBlank
    @Size(max = 255)
    @NotNull
    private String name;
    @Email(message = "Некорректный email")
    @NotNull(message = "Поле email обязательно")
    private String email;
}
