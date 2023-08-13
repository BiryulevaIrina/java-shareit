package ru.practicum.shareit.booking.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingSaveDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class BookingServiceImplTest {
    @InjectMocks
    private BookingServiceImpl bookingService;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private UserService userService;
    @Mock
    private ItemRepository itemRepository;
    private Booking booking;
    private BookingSaveDto bookingSaveDto;
    private UserDto userDto;
    private User user;
    private Item item;
    private Long userId;
    private Long ownerId;
    private Long bookingId;
    private int from;
    private int size;

    @BeforeEach
    void setUp() {
        userId = 1L;
        ownerId = 3L;
        userDto = new UserDto(userId, "user", "user@email.ru");
        user = new User(userId, "user", "user@email.ru");
        item = new Item(1L, "item", "item description", true, user, new ItemRequest());
        bookingId = 1L;
        LocalDateTime start = LocalDateTime.now().plusHours(1);
        LocalDateTime end = LocalDateTime.now().plusDays(1);
        booking = new Booking(bookingId, start, end, item, user, Status.WAITING);
        bookingSaveDto = new BookingSaveDto(1L, start, end, 1L);
        from = 1;
        size = 1;
    }

    @Test
    void createBookingTest() {
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);
        when(userService.getUserById(any(Long.class))).thenReturn(userDto);
        when(itemRepository.findById(any(Long.class))).thenReturn(Optional.of(item));

        BookingDto bookingDto = bookingService.create(ownerId, bookingSaveDto);

        assertEquals(booking.getId(), bookingDto.getId());
        assertEquals(booking.getStart().format(DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss")),
                bookingDto.getStart().format(DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss")));
        assertEquals(booking.getEnd().format(DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss")),
                bookingDto.getEnd().format(DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss")));
        assertEquals(user.getName(), bookingDto.getBooker().getName());
        assertEquals(user.getEmail(), bookingDto.getBooker().getEmail());


        Item item2 = new Item(1L, "item", "item description", false, user, new ItemRequest());
        BookingSaveDto booking1 = new BookingSaveDto(1L, LocalDateTime.now().plusHours(1),
                LocalDateTime.now().minusDays(1), item2.getId());
        BookingSaveDto booking2 = new BookingSaveDto(1L, LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusHours(2), item2.getId());

        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item2));

        assertThrows(BadRequestException.class, () -> bookingService.create(2L, bookingSaveDto));
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        assertThrows(NotFoundException.class, () -> bookingService.create(1L, bookingSaveDto));
        assertThrows(BadRequestException.class, () -> bookingService.create(2L, booking1));

        Throwable thrown = assertThrows(BadRequestException.class,
                () -> bookingService.create(2L, booking2));
        assertEquals("Некорректное введение времени бронирования.", thrown.getMessage());
    }

    @Test
    void updateTest() {
        when(bookingRepository.findById(any(Long.class))).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        BookingDto bookingDto = bookingService.update(userId, bookingId, false);

        assertEquals(Status.REJECTED, bookingDto.getStatus());

        BookingDto bookingDto2 = bookingService.update(userId, bookingId, true);

        assertEquals(Status.APPROVED, bookingDto2.getStatus());

        assertThrows(BadRequestException.class, () -> bookingService.update(userId, bookingId, false));
        assertThrows(NotFoundException.class, () -> bookingService.update(2L, bookingId, true));
        assertThrows(NotFoundException.class, () -> bookingService.update(2L, bookingId, false));
    }

    @Test
    void getByIdTest() {
        when(bookingRepository.findById(any(Long.class))).thenReturn(Optional.of(booking));

        BookingDto bookingDto = bookingService.getById(userId, bookingId);

        assertEquals(booking.getId(), bookingDto.getId());
        assertEquals(booking.getStart().format(DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss")),
                bookingDto.getStart().format(DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss")));
        assertEquals(booking.getEnd().format(DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss")),
                bookingDto.getEnd().format(DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss")));
        assertEquals(user.getName(), bookingDto.getBooker().getName());
        assertEquals(user.getEmail(), bookingDto.getBooker().getEmail());
        assertEquals(Status.WAITING, bookingDto.getStatus());

    }

    @Test
    void getByIdIfUserIsNotBookerAndNotOwner() {
        when(userService.getUserById(any(Long.class))).thenReturn(userDto);
        when(bookingRepository.findById(any(Long.class))).thenReturn(Optional.of(booking));

        Throwable thrown = catchThrowable(() -> bookingService.getById(2L, bookingId));
        assertThat(thrown).isInstanceOf(NotFoundException.class);
        assertThat(thrown.getMessage()).isNotBlank();
        assertEquals("Пользователь с ID = {}" + 2L + " не имеет доступа "
                + "к просмотру данных о бронировании вещи с ID = " + bookingId, thrown.getMessage());
    }

    @Test
    void getBookings() {
        Pageable pageable = PageRequest.of(1, 1);
        when(userService.getUserById(userDto.getId())).thenReturn(userDto);
        when(bookingRepository.findByBookerIdOrderByStartDesc(userDto.getId(), pageable))
                .thenReturn(List.of(booking));

        List<BookingDto> bookingDtos
                = bookingService.getBookings(from, size, userDto.getId(), "ALL");

        assertNotNull(bookingDtos);
        assertEquals(1, bookingDtos.size());

        BookingDto bookingDto = bookingDtos.get(0);

        assertEquals(booking.getId(), bookingDto.getId());
        assertEquals(booking.getStart().format(DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss")),
                bookingDto.getStart().format(DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss")));
        assertEquals(booking.getEnd().format(DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss")),
                bookingDto.getEnd().format(DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss")));
        assertEquals(user.getName(), bookingDto.getBooker().getName());
        assertEquals(user.getEmail(), bookingDto.getBooker().getEmail());

        assertThrows(BadRequestException.class,
                () -> bookingService.getBookings(from, size, userDto.getId(), "ALLL"));
    }

    @Test
    void getOwnerBookings() {
        Pageable pageable = PageRequest.of(1, 1);
        when(userService.getUserById(any(Long.class))).thenReturn(userDto);
        when(bookingRepository.findByItemOwnerIdOrderByStartDesc(userDto.getId(), pageable))
                .thenReturn(List.of(booking));

        List<BookingDto> bookingDtos
                = bookingService.getOwnerBookings(from, size, userDto.getId(), "ALL");

        assertNotNull(bookingDtos);
        assertEquals(1, bookingDtos.size());

        BookingDto bookingDto = bookingDtos.get(0);
        assertEquals(booking.getId(), bookingDto.getId());
        assertEquals(booking.getStart().format(DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss")),
                bookingDto.getStart().format(DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss")));
        assertEquals(booking.getEnd().format(DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss")),
                bookingDto.getEnd().format(DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss")));
        assertEquals(user.getName(), bookingDto.getBooker().getName());
        assertEquals(user.getEmail(), bookingDto.getBooker().getEmail());

        assertThrows(BadRequestException.class,
                () -> bookingService.getOwnerBookings(from, size, userDto.getId(), "ALLL"));

    }
}