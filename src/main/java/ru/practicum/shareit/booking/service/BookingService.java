package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingSaveDto;

import java.util.List;

public interface BookingService {

    BookingDto create(Long userId, BookingSaveDto bookingDto);

    BookingDto update(Long userId, Long bookingId, Boolean approved);

    BookingDto getById(Long userId, Long bookingId);

    List<BookingDto> getBookings(int from, int size, Long userId, String state);

    List<BookingDto> getOwnerBookings(int from, int size, Long userId, String state);

}
