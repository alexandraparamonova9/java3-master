package ru.itmo.web.student;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record StudentRequestDto(
        @NotBlank(message = "ФИО не может быть пустым")
        @Pattern(regexp = "^\\D*$",
                message = "ФИО не должно содержать цифры")
        String fullName,
        @NotBlank(message = "Группа обязательна")
        @Pattern(regexp = "^\\p{L}\\d{4}$",
                message = "Группа должна иметь формат: 1 буква + 4 цифры")
        String groupName,
        @NotNull(message = "ИСУ ID обязателен")
        @Positive(message = "ИСУ ID должен быть положительным")
        Integer isuId,
        @NotNull(message = "Номер общежития обязателен")
        @Positive(message = "Номер общежития должен быть положительным")
        Integer dormNumber,
        @NotNull(message = "Номер комнаты обязателен")
        @Positive(message = "Номер комнаты должен быть положительным")
        Integer room,
        @NotNull(message = "Дата заселения обязательна")
        LocalDate moveInDate,
        @NotNull(message = "Поле должно иметь значение true или false")
        Boolean isForeigner,
        @Size(max = 2000, message = "Заметки не длиннее 2000 символов")
        String notes

) {
}
