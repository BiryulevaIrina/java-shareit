package ru.practicum.shareit.booking.service;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.dto.BookingSaveDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;
    private final UserService userService;

    @Override
    public BookingDto create(Long userId, BookingSaveDto bookingDto) {
        Booking booking = mapToNewBooking(bookingDto, userId);
        Item item = itemRepository.findById(booking.getItem().getId())
                .orElseThrow(() -> new NotFoundException("Не найдена вещь с ID = "
                        + bookingDto.getItemId()));
        getUser(userId);
        if (Objects.equals(item.getOwner().getId(), userId)) {
            throw new NotFoundException("Собственник вещи не может ее забронировать.");
        }
        if (!item.getAvailable()) {
            throw new BadRequestException("Вещь не доступна для бронирования.");
        }
        if (bookingDto.getStart() == null || bookingDto.getEnd() == null ||
                bookingDto.getStart().isAfter(bookingDto.getEnd()) ||
                bookingDto.getStart().equals(bookingDto.getEnd()) ||
                bookingDto.getEnd().isBefore(bookingDto.getStart()) ||
                bookingDto.getStart().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Некорректное введение времени бронирования.");
        }
        return BookingMapper.toBookingDto(bookingRepository.save(booking));
    }

    @Override
    public BookingDto update(Long userId, Long bookingId, Boolean approved) {
        getUser(userId);
        Booking booking = getBooking(bookingId);
        if (!Objects.equals(booking.getItem().getOwner().getId(), userId)) {
            throw new NotFoundException("Пользователь с ID = " + userId + " не является собственником вещи.");
        }
        if (booking.getStatus().equals(Status.APPROVED)) {
            throw new BadRequestException("Статус бронирования вещи уже установлен APPROVED");
        }
        booking.setStatus(!approved ? Status.REJECTED : Status.APPROVED);
        return BookingMapper.toBookingDto(bookingRepository.save(booking));
    }

    @Override
    public BookingDto getById(Long userId, Long bookingId) {
        getUser(userId);
        Booking booking = getBooking(bookingId);
        if (!Objects.equals(booking.getBooker().getId(), userId) &&
                !Objects.equals(booking.getItem().getOwner().getId(), userId)) {
            throw new NotFoundException("Пользователь с ID = " + userId + " не имеет доступа "
                    + "к просмотру данных о бронировании вещи с ID = " + bookingId);
        }
        return BookingMapper.toBookingDto(booking);
    }

    @Override
    public List<BookingDto> getBookings(int from, int size, Long userId, String state) {
        getUser(userId);
        Pageable pageable = PageRequest.of(from / size, size);
        List<Booking> bookings;
        switch (state) {
            case "ALL":
                bookings = bookingRepository.findByBookerIdOrderByStartDesc(userId, pageable);
                break;
            case "CURRENT":
                bookings = bookingRepository.findByBookerIdAndStartIsBeforeAndEndIsAfterOrderByStartDesc(userId,
                        LocalDateTime.now(), LocalDateTime.now(), pageable);
                break;
            case "PAST":
                bookings = bookingRepository.findByBookerIdAndEndIsBeforeOrderByStartDesc(userId,
                        LocalDateTime.now(), pageable);
                break;
            case "FUTURE":
                bookings = bookingRepository.findByBookerIdAndStartIsAfterOrderByStartDesc(userId,
                        LocalDateTime.now(), pageable);
                break;
            case "WAITING":
                bookings = bookingRepository.findByBookerIdAndStatusOrderByStartDesc(userId, Status.WAITING, pageable);
                break;
            case "REJECTED":
                bookings = bookingRepository.findByBookerIdAndStatusOrderByStartDesc(userId, Status.REJECTED, pageable);
                break;
            default:
                throw new BadRequestException("Unknown state: UNSUPPORTED_STATUS");
        }
        return bookings.stream()
                .map(BookingMapper::toBookingDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingDto> getOwnerBookings(int from, int size, Long userId, String state) {
        getUser(userId);
        Pageable pageable = PageRequest.of(from / size, size);
        List<Booking> bookings;
        switch (state) {
            case "ALL":
                bookings = bookingRepository.findByItemOwnerIdOrderByStartDesc(userId, pageable);
                break;
            case "CURRENT":
                bookings = bookingRepository.findByItemOwnerIdAndStartIsBeforeAndEndIsAfterOrderByStartDesc(userId,
                        LocalDateTime.now(), LocalDateTime.now(), pageable);
                break;
            case "PAST":
                bookings = bookingRepository.findByItemOwnerIdAndEndIsBeforeOrderByStartDesc(userId,
                        LocalDateTime.now(), pageable);
                break;
            case "FUTURE":
                bookings = bookingRepository.findByItemOwnerIdAndStartIsAfterOrderByStartDesc(userId,
                        LocalDateTime.now(), pageable);
                break;
            case "WAITING":
                bookings = bookingRepository.findByItemOwnerIdAndStatusOrderByStartDesc(userId,
                        Status.WAITING, pageable);
                break;
            case "REJECTED":
                bookings = bookingRepository.findByItemOwnerIdAndStatusOrderByStartDesc(userId,
                        Status.REJECTED, pageable);
                break;
            default:
                throw new BadRequestException("Unknown state: UNSUPPORTED_STATUS");
        }
        return bookings.stream()
                .map(BookingMapper::toBookingDto)
                .collect(Collectors.toList());
    }

    private void getUser(Long userId) {
        userService.getUserById(userId);
    }

    private Booking getBooking(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Не найдено бронирование с ID "
                        + bookingId));
    }

    private Booking mapToNewBooking(BookingSaveDto bookingSaveDto, Long bookerId) {
        Booking booking = new Booking();
        booking.setStart(bookingSaveDto.getStart());
        booking.setEnd(bookingSaveDto.getEnd());
        booking.setItem(itemRepository.findById(bookingSaveDto.getItemId())
                .orElseThrow(() -> new NotFoundException("Вещи с ID " + bookingSaveDto.getItemId()
                        + " нет в базе.")));
        booking.setBooker(UserMapper.toUser(userService.getUserById(bookerId)));
        booking.setStatus(Status.WAITING);
        return booking;
    }

}
