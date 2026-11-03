package ru.itmo.web.auth;
import jakarta.validation.constraints.NotBlank;
public record LoginRequestDto(
        @NotBlank(message = "логин не может быть пустым")
        String username,
        @NotBlank(message = "лароль не может быть пустым")
        String password
) {
}