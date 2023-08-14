package ru.practicum.shareit.item.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.CommentRepository;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemBookingDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ItemServiceImplTest {
    @InjectMocks
    private ItemServiceImpl itemService;
    @Mock
    private UserService userService;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private ItemRequestRepository itemRequestRepository;

    private ItemDto itemDto;
    private Item item;
    private User user;
    private UserDto userDto;
    private Booking booking;
    private Booking lastBooking;
    private Booking nextBooking;
    private ItemRequest itemRequest;
    private Comment comment;
    private CommentDto commentDto;
    private int from;
    private int size;

    @BeforeEach
    void setUp() {
        user = new User(1L, "user", "user@email.ru");
        userDto = new UserDto(1L, "user", "user@email.ru");
        item = new Item(1L, "item", "item description", true, user, new ItemRequest());
        itemDto = new ItemDto(1L, "item", "item description", true, 1L, 1L);
        booking = new Booking(1L, LocalDateTime.now().minusDays(3),
                LocalDateTime.now().minusDays(1).plusHours(1),
                item, user, Status.APPROVED);
        lastBooking = new Booking(4L, LocalDateTime.now().minusDays(1),
                LocalDateTime.now().minusHours(5),
                item, user, Status.APPROVED);
        nextBooking = new Booking(5L, LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusDays(1),
                item, user, Status.APPROVED);
        commentDto = new CommentDto(1L, "comment", item, "userName", LocalDateTime.now());
        comment = new Comment(1L, "comment", item, user, LocalDateTime.now());

        itemRequest = new ItemRequest(1L, "description", LocalDateTime.now(), user);
        from = 1;
        size = 1;
    }

    @Test
    void getItems() {
        Pageable pageable = PageRequest.of(from / size, size);
        Item item = new Item(1L, "item", "item test", true, user, null);

        when(itemRepository.findByOwnerId(1L, pageable)).thenReturn(Collections.singletonList(item));
        when(bookingRepository.findAllByItemIdAndStatusOrderByEndAsc(1L, Status.APPROVED))
                .thenReturn(List.of(lastBooking, nextBooking));

        final List<ItemBookingDto> itemDtos = itemService.getItems(from, size, user.getId());

        assertNotNull(itemDtos);
        assertEquals(1, itemDtos.size());

        ItemBookingDto itemDto = itemDtos.get(0);

        assertEquals(item.getId(), itemDto.getId());
        assertEquals(item.getName(), itemDto.getName());
        assertEquals(item.getDescription(), itemDto.getDescription());
        assertNotNull(itemDto.getLastBooking());
        assertNotNull(itemDto.getNextBooking());

        assertThrows(NotFoundException.class, () -> itemService.getItems(from, size, 2L));
        assertThrows(BadRequestException.class, () -> itemService.getItems(-1, size, 2L));
        assertThrows(BadRequestException.class, () -> itemService.getItems(from, -1, 2L));
    }

    @Test
    void createItemTest() {
        when(userService.getUserById(userDto.getId())).thenReturn(userDto);
        when(itemRequestRepository.findById(itemRequest.getId())).thenReturn(Optional.of(itemRequest));
        when(itemRepository.save(any())).thenReturn(item);

        ItemDto newItem = itemService.create(1L, itemDto);

        assertEquals(newItem.getId(), itemDto.getId());
        assertEquals(newItem.getName(), itemDto.getName());
        assertEquals(newItem.getDescription(), itemDto.getDescription());
    }

    @Test
    void createItemWhenRequestIdIsNullTest() {
        ItemDto itemDto = new ItemDto(1L, "item", "item description",
                true, 1L, null);
        Item item = new Item(1L, "item", "item description", true, user, null);

        when(userService.getUserById(userDto.getId())).thenReturn(userDto);
        when(itemRepository.save(any())).thenReturn(item);

        assertNull(itemService.create(1L, itemDto).getRequestId());
    }

    @Test
    void createItemIfNameNullTest() {
        ItemDto itemDto = new ItemDto(1L, null, "item description",
                true, 1L, 1L);

        Throwable thrown = catchThrowable(() -> itemService.create(1L, itemDto));

        assertThat(thrown).isInstanceOf(BadRequestException.class);
        assertThat(thrown.getMessage()).isNotBlank();
        assertEquals("Наименование вещи не может быть пустым", thrown.getMessage());
    }

    @Test
    void createItemIfNameIsBlankTest() {
        ItemDto itemDto = new ItemDto(1L, " ", "item description",
                true, 1L, 1L);

        Throwable thrown = catchThrowable(() -> itemService.create(1L, itemDto));

        assertThat(thrown).isInstanceOf(BadRequestException.class);
        assertThat(thrown.getMessage()).isNotBlank();
        assertEquals("Наименование вещи не может быть пустым", thrown.getMessage());
    }

    @Test
    void createItemIfDescriptionNullTest() {
        ItemDto itemDto = new ItemDto(1L, "item", null,
                true, 1L, 1L);

        Throwable thrown = catchThrowable(() -> itemService.create(1L, itemDto));

        assertThat(thrown).isInstanceOf(BadRequestException.class);
        assertThat(thrown.getMessage()).isNotBlank();
        assertEquals("Описание вещи не может быть пустым", thrown.getMessage());
    }

    @Test
    void createItemIfDescriptionIsBlankTest() {
        ItemDto itemDto = new ItemDto(1L, "item", " ",
                true, 1L, 1L);

        Throwable thrown = catchThrowable(() -> itemService.create(1L, itemDto));

        assertThat(thrown).isInstanceOf(BadRequestException.class);
        assertThat(thrown.getMessage()).isNotBlank();
        assertEquals("Описание вещи не может быть пустым", thrown.getMessage());
    }

    @Test
    void createItemIfAvailableNullTest() {
        ItemDto itemDto = new ItemDto(1L, "item", "item description",
                null, 1L, 1L);

        Throwable thrown = catchThrowable(() -> itemService.create(1L, itemDto));

        assertThat(thrown).isInstanceOf(BadRequestException.class);
        assertThat(thrown.getMessage()).isNotBlank();
        assertEquals("Статус доступности вещи должен быть установлен", thrown.getMessage());
    }

    @Test
    void updateItemTest() {
        when(userService.getUserById(userDto.getId())).thenReturn(userDto);
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));

        ItemDto updatedItem = itemService.update(1L, 1L, itemDto);

        assertEquals(item.getId(), updatedItem.getId());
        assertEquals(item.getName(), updatedItem.getName());
        assertEquals(item.getDescription(), updatedItem.getDescription());

        assertThrows(NotFoundException.class, () -> itemService.update(1L, 2L, itemDto));
        assertThrows(NotFoundException.class, () -> itemService.update(2L, 1L, itemDto));
    }

    @Test
    void getItemByIdTest() {
        when(bookingRepository.findAllByItemIdAndStatusOrderByEndAsc(item.getId(), Status.APPROVED))
                .thenReturn(List.of(lastBooking, nextBooking));
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));

        ItemBookingDto itemDto = itemService.getItemById(1L, 1L);

        assertEquals(item.getId(), itemDto.getId());
        assertEquals(item.getName(), itemDto.getName());
        assertEquals(item.getDescription(), itemDto.getDescription());
        assertNotNull(itemDto.getLastBooking());
        assertNotNull(itemDto.getNextBooking());
    }

    @Test
    void searchItemTest() {
        assertEquals(itemService.searchItem(from, size, ""), List.of());

        assertThrows(BadRequestException.class,
                () -> itemService.searchItem(-1, size, "text"));

        assertThrows(BadRequestException.class,
                () -> itemService.searchItem(from, -1, "text"));

    }

    @Test
    void createCommentTest() {
        when(bookingRepository.findAllByItemIdAndBookerIdAndStatus(any(Long.class), any(Long.class), any(Status.class)))
                .thenReturn(List.of(booking));
        when(userService.getUserById(any(Long.class))).thenReturn(userDto);
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);

        CommentDto newCommentDto = itemService.createComment(1L, 1L, commentDto);

        assertEquals(commentDto.getId(), newCommentDto.getId());
        assertEquals(commentDto.getText(), newCommentDto.getText());
        assertEquals(user.getName(), newCommentDto.getAuthorName());
        assertEquals(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm")),
                newCommentDto.getCreated().format(DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm")));

    }

    @Test
    void createCommentIfTextNullTest() {
        commentDto = new CommentDto(1L, null, item, "userName", LocalDateTime.now());
        when(bookingRepository.findAllByItemIdAndBookerIdAndStatus(any(Long.class), any(Long.class), any(Status.class)))
                .thenReturn(List.of(booking));

        Throwable thrown = catchThrowable(() -> itemService.createComment(1L, 1L, commentDto));

        assertThat(thrown).isInstanceOf(BadRequestException.class);
        assertThat(thrown.getMessage()).isNotBlank();
        assertEquals("Текст комментария не может быть пустым", thrown.getMessage());
    }

    @Test
    void createCommentIfTextIsBlankTest() {
        commentDto = new CommentDto(1L, " ", item, "userName", LocalDateTime.now());
        when(bookingRepository.findAllByItemIdAndBookerIdAndStatus(any(Long.class), any(Long.class), any(Status.class)))
                .thenReturn(List.of(booking));

        Throwable thrown = catchThrowable(() -> itemService.createComment(1L, 1L, commentDto));

        assertThat(thrown).isInstanceOf(BadRequestException.class);
        assertThat(thrown.getMessage()).isNotBlank();
        assertEquals("Текст комментария не может быть пустым", thrown.getMessage());

    }
}
