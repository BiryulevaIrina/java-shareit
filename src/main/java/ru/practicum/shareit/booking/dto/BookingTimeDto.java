package ru.practicum.shareit.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookingTimeDto {
    private Long id;
    private Long bookerId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
