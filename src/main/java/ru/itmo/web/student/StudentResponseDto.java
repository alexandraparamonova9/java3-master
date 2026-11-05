package ru.itmo.web.student;
import java.time.LocalDate;
public record StudentResponseDto(
        Long id,
        String fullName,
        String groupName,
        Integer isuId,
        Integer dormNumber,
        Integer room,
        LocalDate moveInDate,
        Boolean isForeigner,
        String notes
) {
    public static StudentResponseDto from(Student student) {
        return new StudentResponseDto(
                student.getId(),
                student.getFullName(),
                student.getGroupName(),
                student.getIsuId(),
                student.getDormNumber(),
                student.getRoom(),
                student.getMoveInDate(),
                student.getForeigner(),
                student.getNotes()
        );
    }
}