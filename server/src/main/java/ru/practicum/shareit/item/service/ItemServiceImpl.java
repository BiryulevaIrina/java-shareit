package ru.practicum.shareit.item.service;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.CommentRepository;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserService userService;
    private final CommentRepository commentRepository;
    private final BookingRepository bookingRepository;
    private final ItemRequestRepository itemRequestRepository;

    @Override
    public List<ItemBookingDto> getItems(int from, int size, Long userId) {
        Pageable pageable = PageRequest.of(from / size, size);
        List<Item> items = itemRepository.findByOwnerId(userId, pageable);
        if (items.isEmpty()) {
            throw new NotFoundException("У пользователя не найдена такая вещь.");
        }
        return items.stream()
                .map(item -> mapToItemBookingDto(item, userId, item.getId()))
                .collect(Collectors.toList());
    }

    @Override
    public ItemDto create(Long userId, ItemDto itemDto) {
        throwIfNotValid(itemDto);
        User owner = UserMapper.toUser(userService.getUserById(userId));
        Item item = ItemMapper.toItem(itemDto);
        item.setOwner(owner);
        setRequest(item, itemDto);
        return ItemMapper.toItemDto(itemRepository.save(item));
    }

    @Override
    public ItemDto update(Long userId, Long itemId, ItemDto itemDto) {
        Item item = getItem(userId, itemId);
        if (!Objects.equals(item.getOwner().getId(), userId)) {
            throw new NotFoundException("У пользователя не найдена такая вещь.");
        }
        if (itemDto.getAvailable() != null) {
            item.setAvailable(itemDto.getAvailable());
        }
        if (itemDto.getName() != null) {
            item.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null) {
            item.setDescription(itemDto.getDescription());
        }
        itemRepository.save(item);
        return ItemMapper.toItemDto(item);
    }

    @Override
    public ItemBookingDto getItemById(Long userId, Long itemId) {
        return itemRepository.findById(itemId)
                .map((Item item) -> mapToItemBookingDto(item, userId, itemId))
                .orElseThrow(() -> new NotFoundException("Не найдена вещь с идентификатором " + itemId));
    }

    @Override
    public List<ItemDto> searchItem(int from, int size, String text) {
        if (text.isBlank()) {
            return List.of();
        }
        Pageable pageable = PageRequest.of(from / size, size);
        return itemRepository.searchItem(text, pageable).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public CommentDto createComment(Long userId, Long itemId, CommentDto commentDto) {
        if (getBookings(userId, itemId).isEmpty()) {
            throw new BadRequestException("Пользователь с ID = " + userId + " вещь с ID = "
                    + itemId + " не бронировал");
        }
        if (commentDto.getText() == null || commentDto.getText().isBlank()) {
            throw new BadRequestException("Текст комментария не может быть пустым");
        }
        Comment comment = CommentMapper.toComment(commentDto);
        comment.setId(comment.getId());
        comment.setText(comment.getText());
        comment.setItem(getItem(userId, itemId));
        comment.setAuthor(UserMapper.toUser(userService.getUserById(userId)));
        comment.setCreated(LocalDateTime.now());
        return CommentMapper.toCommentDto(commentRepository.save(comment));
    }

    private List<CommentDto> getCommentsByItemId(Long itemId) {
        return commentRepository.findAllByItemId(itemId).stream()
                .map(CommentMapper::toCommentDto)
                .collect(Collectors.toList());
    }

    private Item getItem(Long userId, Long itemId) {
        userService.getUserById(userId);
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь с идентификатором " + itemId
                        + " отсутствует в базе."));
    }

    private void throwIfNotValid(ItemDto itemDto) {
        if ((itemDto.getName() == null) || itemDto.getName().isBlank()) {
            throw new BadRequestException("Наименование вещи не может быть пустым");
        }
        if ((itemDto.getDescription() == null) || itemDto.getDescription().isBlank()) {
            throw new BadRequestException("Описание вещи не может быть пустым");
        }
        if (itemDto.getAvailable() == null) {
            throw new BadRequestException("Статус доступности вещи должен быть установлен");
        }
    }

    private ItemBookingDto mapToItemBookingDto(Item item, Long userId, Long itemId) {
        ItemBookingDto itemBookingDto = ItemMapper.toItemBookingDto(item);
        List<Booking> bookings = bookingRepository
                .findAllByItemIdAndStatusOrderByEndAsc(itemId, Status.APPROVED);
        if (bookings.isEmpty() || !Objects.equals(item.getOwner().getId(), userId)) {
            itemBookingDto.setLastBooking(ItemMapper.toBookingDateDto(new Booking()));
            itemBookingDto.setNextBooking(ItemMapper.toBookingDateDto(new Booking()));
        } else {
            Booking last = bookings.get(0);
            Booking next = bookings.get(bookings.size() - 1);
            if (bookings.size() == 1) {
                itemBookingDto.setLastBooking(ItemMapper.toBookingDateDto(last));
                itemBookingDto.setNextBooking(ItemMapper.toBookingDateDto(new Booking()));
            } else {
                for (Booking booking : bookings) {
                    if (booking.getEnd().isBefore(LocalDateTime.now())) {
                        if (booking.getEnd().isAfter(last.getEnd())) {
                            last = booking;
                        }
                    }
                    if (booking.getStart().isAfter(LocalDateTime.now())) {
                        if (booking.getEnd().isBefore(next.getEnd())) {
                            next = booking;
                        }
                    }
                }
                itemBookingDto.setLastBooking(ItemMapper.toBookingDateDto(last));
                itemBookingDto.setNextBooking(ItemMapper.toBookingDateDto(next));
            }
        }
        itemBookingDto.setComments(getCommentsByItemId(item.getId()));
        return itemBookingDto;
    }

    private List<Booking> getBookings(Long userId, Long itemId) {
        return bookingRepository.findAllByItemIdAndBookerIdAndStatus(itemId, userId, Status.APPROVED)
                .stream()
                .filter(booking -> booking.getEnd().isBefore(LocalDateTime.now()))
                .collect(Collectors.toList());
    }

    private void setRequest(Item item, ItemDto itemDto) {
        if (itemDto.getRequestId() == null) {
            item.setRequest(null);
        } else {
            item.setRequest(itemRequestRepository.findById(itemDto.getRequestId())
                    .orElseThrow(() -> new NotFoundException("Запрос с идентификатором " + itemDto.getRequestId()
                            + " не найден.")));
        }
    }
}
