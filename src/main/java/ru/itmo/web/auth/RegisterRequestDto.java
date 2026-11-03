package ru.itmo.web.auth;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
public record RegisterRequestDto(
        @NotBlank(message = "логин не может быть пустым")
        @Size(min = 3, max = 50,
                message = "логин должен содержать от 3 до 50 символов")
        String username,
        @NotBlank(message = "Email не может быть пустым")
        @Email(message = "некорректный формат email")
        String email,
        @NotBlank(message = "пароль не может быть пустым")
        @Size(min = 8,
                message = "пароль должен содержать минимум 8 символов")
        String password
) {
}