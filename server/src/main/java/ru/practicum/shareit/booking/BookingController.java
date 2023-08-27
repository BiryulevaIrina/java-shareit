package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingSaveDto;
import ru.practicum.shareit.booking.service.BookingService;

import java.util.List;

/**
 * TODO Sprint add-bookings.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/bookings")
@Slf4j
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public BookingDto createNewBooking(@RequestHeader("X-Sharer-User-Id") Long userId,
                                       @RequestBody BookingSaveDto bookingDto) {
        log.info("Получен POST-запрос на бронирование пользователем с ID={}", userId);
        return bookingService.create(userId, bookingDto);
    }

    @PatchMapping("/{bookingId}")
    public BookingDto updateStatus(@RequestHeader("X-Sharer-User-Id") Long userId,
                                   @PathVariable Long bookingId,
                                   @RequestParam Boolean approved) {
        log.info("Получен PATCH-запрос пользователя с ID={} на подтверждение или отклонение запроса " +
                "на бронирование вещи владельцем с ID={}", bookingId, userId);
        return bookingService.update(userId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public BookingDto getBookingById(@RequestHeader("X-Sharer-User-Id") Long userId, @PathVariable Long bookingId) {
        log.info("Получен GET-запрос на получение данных о бронировании с ID={} пользователем с ID={}",
                bookingId, userId);
        return bookingService.getById(userId, bookingId);
    }

    @GetMapping
    public List<BookingDto> getBookings(@RequestHeader("X-Sharer-User-Id") Long userId,
                                        @RequestParam(value = "state", defaultValue = "ALL",
                                                required = false) String state,
                                        @RequestParam(defaultValue = "0") int from,
                                        @RequestParam(defaultValue = "10") int size) {
        log.info("Получен GET-запрос на получение списка всех бронирований пользователя при state {}, userId={}, " +
                "from={}, size={}", state, userId, from, size);
        return bookingService.getBookings(from, size, userId, state);
    }

    @GetMapping("/owner")
    public List<BookingDto> getOwnerBookings(@RequestHeader("X-Sharer-User-Id") Long userId,
                                             @RequestParam(value = "state", defaultValue = "ALL",
                                                     required = false) String state,
                                             @RequestParam(defaultValue = "0") int from,
                                             @RequestParam(defaultValue = "10") int size) {
        log.info("Получен GET-запрос на получение списка всех бронирований пользователя (владельца)" +
                " при state {}, userId={}, from={}, size={}", state, userId, from, size);
        return bookingService.getOwnerBookings(from, size, userId, state);
    }
}
