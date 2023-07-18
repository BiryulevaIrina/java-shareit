package ru.practicum.shareit.booking.dto;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.dto.UserMapper;

import javax.validation.constraints.NotNull;

@Component
@AllArgsConstructor
public class BookingMapper {
    private ItemRepository itemRepository;
    private UserRepository userRepository;

    public BookingDto toBookingDto(@NotNull Booking booking) {
        return new BookingDto(
                booking.getId(),
                booking.getStart(),
                booking.getEnd(),
                ItemMapper.toItemDto(booking.getItem()),
                UserMapper.toUserDto(booking.getBooker()),
                booking.getStatus()
        );
    }

    public BookingTimeDto toBookingTimeDto(@NotNull Booking booking) {
        return new BookingTimeDto(
                booking.getId(),
                booking.getBooker().getId(),
                booking.getStart(),
                booking.getEnd()
        );
    }

    public Booking maptoNewBooking(BookingSaveDto bookingSaveDto, Long bookerId) {
        Booking booking = new Booking();
        booking.setStart(bookingSaveDto.getStart());
        booking.setEnd(bookingSaveDto.getEnd());
        booking.setItem(itemRepository.findById(bookingSaveDto.getItemId())
                .orElseThrow(() -> new NotFoundException("Вещи с ID " + bookingSaveDto.getItemId()
                        + " нет в базе.")));
        booking.setBooker(userRepository.findById(bookerId)
                .orElseThrow(() -> new NotFoundException("Пользователя с ID " + bookerId
                        + " нет в базе.")));
        booking.setStatus(Status.WAITING);
        return booking;
    }

}
