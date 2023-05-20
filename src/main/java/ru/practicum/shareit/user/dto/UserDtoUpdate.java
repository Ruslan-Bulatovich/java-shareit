package ru.practicum.shareit.user.dto;

import lombok.Builder;
import lombok.Getter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Builder
@Getter
public class UserDtoUpdate {
    @NotBlank
    @Size(max = 255)
    private String name;
    @Email(message = "Некорректный email")
    private String email;
}
