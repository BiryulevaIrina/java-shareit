package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import ru.practicum.shareit.booking.dto.BookingDateDto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class ItemBookingDto {
    private long id;
    @NotNull
    @NotBlank
    private String name;
    @NotBlank
    private String description;
    @NotNull
    private Boolean available;
    private BookingDateDto lastBooking;
    private BookingDateDto nextBooking;
    private List<CommentDto> comments;
}
