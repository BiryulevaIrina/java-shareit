package ru.practicum.shareit.item.dto;

import lombok.*;
import ru.practicum.shareit.booking.dto.BookingTimeDto;

@EqualsAndHashCode(callSuper = true)
@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class ItemBookingDto extends ItemDto {
    private BookingTimeDto lastBooking;
    private BookingTimeDto nextBooking;
}
