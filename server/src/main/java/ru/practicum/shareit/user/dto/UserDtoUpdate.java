package ru.practicum.shareit.user.dto;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class UserDtoUpdate {
    private String name;
    private String email;
}
