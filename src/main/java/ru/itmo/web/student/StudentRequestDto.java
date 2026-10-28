package ru.itmo.web.student;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
public record StudentRequestDto(
        @NotBlank(message = "ФИО не может быть пустым")
        @Pattern(regexp = "^\\D*$", message = "ФИО не должно содержать цифры")
        String fullName,
        @NotBlank(message = "группа обязательна")
        @Pattern(regexp = "^\\p{L}\\d{4}$",
                message = "группа должна иметь формат: 1 буква + 4 цифры")
        String groupName,
        @NotBlank(message = "ИСУ ID обязателен")
        @Pattern(regexp = "^[1-9]\\d*$",
                message = "ИСУ ID должен быть положительным целым числом")
        String isuId,
        @NotNull(message = "номер общежития обязателен")
        @Positive(message = "номер общежития должен быть положительным")
        Integer dormNumber,
        @NotBlank(message = "номер комнаты обязателен")
        @Pattern(regexp = "^[1-9]\\d*$",
                message = "номер комнаты должен быть положительным целым числом")
        String room,
        @NotNull(message = "дата заселения обязательна")
        LocalDate moveInDate,
        @NotNull(message = "поле должно иметь значение true или false")
        Boolean isForeigner,
        @Size(max = 2000, message = "заметки не длиннее 2000 символов")
        String notes
) {
}