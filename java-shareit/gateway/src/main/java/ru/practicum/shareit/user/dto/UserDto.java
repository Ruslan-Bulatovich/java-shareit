package ru.practicum.shareit.user.dto;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Data
@Builder
public class UserDto {
    @Pattern(regexp = "^[^ ].*[^ ]$", message = "Неккоректное имя")
    @Size(max = 255)
    @NotNull
    private String name;
    @Email(message = "Некорректный email")
    @NotNull(message = "Поле email обязательно")
    private String email;
}
