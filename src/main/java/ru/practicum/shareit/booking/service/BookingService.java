package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingTimeDto;
import ru.practicum.shareit.booking.dto.BookingSaveDto;

import java.util.List;

public interface BookingService {

    BookingDto create(Long userId, BookingSaveDto bookingDto);

    BookingDto update(Long userId, Long bookingId, Boolean approved);

    BookingDto getById(Long userId, Long bookingId);

   List<BookingDto> getBookings(Long userId, String state);

   List<BookingDto> getOwnerBookings(Long userId, String state);

   BookingTimeDto getNextBooking(Long itemId);

    BookingTimeDto getLastBooking(Long itemId);

    //Booking getBookingByIdWithStatus(Long itemId, Long bookerId);
}
