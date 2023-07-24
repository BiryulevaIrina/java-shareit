package ru.practicum.shareit.item.dto;

import lombok.*;
import ru.practicum.shareit.booking.dto.BookingDateDto;

import java.util.List;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class ItemBookingDto {
    private long id;
    private String name;
    private String description;
    private Boolean available;
    private BookingDateDto lastBooking;
    private BookingDateDto nextBooking;
    private List<CommentDto> comments;
}
