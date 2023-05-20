package ru.practicum.shareit.user.dto;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Data
public class UserDto {
    private long id;
    @Email(message = "Поле email заполненно некорректно. Проверьте формат.")
    @NotBlank(message = "Поле email не должно быть пустым.")
    private String email;
    @NotBlank(message = "Поле name не должно быть пустым.")
    private String name;
}