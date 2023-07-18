package ru.practicum.shareit.booking.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.dto.BookingTimeDto;
import ru.practicum.shareit.booking.dto.BookingSaveDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.dto.UserMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingMapper bookingMapper;


    @Autowired
    public BookingServiceImpl(BookingRepository bookingRepository, ItemRepository itemRepository,
                              UserRepository userRepository, BookingMapper bookingMapper) {
        this.bookingRepository = bookingRepository;
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
        this.bookingMapper = bookingMapper;
    }

    @Override
    public BookingDto create(Long userId, BookingSaveDto bookingDto) {
        Booking booking = bookingMapper.maptoNewBooking(bookingDto, userId);
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
        return bookingMapper.toBookingDto(bookingRepository.save(booking));
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
        if (approved) {
            booking.setStatus(Status.APPROVED);
        } else {
            booking.setStatus(Status.REJECTED);
        }
        return bookingMapper.toBookingDto(bookingRepository.save(booking));
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
        return bookingMapper.toBookingDto(booking);
    }

    @Override
    public List<BookingDto> getBookings(Long userId, String state) {
        getUser(userId);
        List<Booking> bookings;
        switch (state) {
            case "ALL":
                bookings = bookingRepository.findByBookerId(userId, sortByStartDesc());
                break;
            case "CURRENT":
                bookings = bookingRepository.findByBookerIdAndStartIsBeforeAndEndIsAfter(userId,
                        LocalDateTime.now(), LocalDateTime.now(), sortByStartDesc());
                break;
            case "PAST":
                bookings = bookingRepository.findByBookerIdAndEndIsBefore(userId,
                        LocalDateTime.now(), sortByStartDesc());
                break;
            case "FUTURE":
                bookings = bookingRepository.findByBookerIdAndStartIsAfter(userId,
                        LocalDateTime.now(), sortByStartDesc());
                break;
            case "WAITING":
                bookings = bookingRepository.findByBookerIdAndStatus(userId, Status.WAITING,
                        sortByStartDesc());
                break;
            case "REJECTED":
                bookings = bookingRepository.findByBookerIdAndStatus(userId, Status.REJECTED,
                        sortByStartDesc());
                break;
            default:
                throw new BadRequestException("Unknown state: UNSUPPORTED_STATUS");
        }
        return bookings.stream().
                map(bookingMapper::toBookingDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingDto> getOwnerBookings(Long userId, String state) {
        getUser(userId);
        List<Booking> bookings;
        switch (state) {
            case "ALL":
                bookings = bookingRepository.findByItemOwnerId(userId, sortByStartDesc());
                break;
            case "CURRENT":
                bookings = bookingRepository.findByItemOwnerIdAndStartIsBeforeAndEndIsAfter(userId,
                        LocalDateTime.now(), LocalDateTime.now(), sortByStartDesc());
                break;
            case "PAST":
                bookings = bookingRepository.findByItemOwnerIdAndEndIsBefore(userId,
                        LocalDateTime.now(), sortByStartDesc());
                break;
            case "FUTURE":
                bookings = bookingRepository.findByItemOwnerIdAndStartIsAfter(userId,
                        LocalDateTime.now(), sortByStartDesc());
                break;
            case "WAITING":
                bookings = bookingRepository.findByItemOwnerIdAndStatus(userId, Status.WAITING,
                        sortByStartDesc());
                break;
            case "REJECTED":
                bookings = bookingRepository.findByItemOwnerIdAndStatus(userId, Status.REJECTED,
                        sortByStartDesc());
                break;
            default:
                throw new BadRequestException("Unknown state: UNSUPPORTED_STATUS");
        }
        return bookings.stream().
                map(bookingMapper::toBookingDto)
                .collect(Collectors.toList());
    }

    @Override
    public BookingTimeDto getNextBooking(Long itemId) {
        return bookingMapper.toBookingTimeDto(bookingRepository.findFirstByItemIdAndStartIsAfter(itemId,
                LocalDateTime.now(), Sort.by(Sort.Direction.ASC, "start")));
    }

    @Override
    public BookingTimeDto getLastBooking(Long itemId) {
        return bookingMapper.toBookingTimeDto(bookingRepository.findFirstByItemIdAndEndIsBefore(itemId,
                LocalDateTime.now(), Sort.by(Sort.Direction.DESC, "end")));
    }

  /*  @Override
    public Booking getBookingByIdWithStatus(Long itemId, Long bookerId) {
        return bookingRepository.findFirstByItemIdAndByBookerIdAndEndIsBeforeAndStatus(itemId, bookerId,
                LocalDateTime.now(), Status.APPROVED);
    }*/


    private void getUser(Long userId) {
        userRepository.findById(userId)
                .map(UserMapper::toUserDto)
                .orElseThrow(() -> new NotFoundException("Пользователя с идентификатором " + userId
                        + " нет в базе."));
    }

    private Booking getBooking(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Не найдено бронирование с ID "
                        + bookingId));
    }

    private Sort sortByStartDesc() {
        return Sort.by(Sort.Direction.DESC, "start");
    }

}
