package ru.practicum.shareit.item.service;

import lombok.AllArgsConstructor;
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
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final BookingRepository bookingRepository;

    @Override
    public List<ItemBookingDto> getItems(Long userId) {
        List<ItemBookingDto> itemBookingDto;
        List<Item> itemList = itemRepository.findByOwnerId(userId);
        if (itemList.isEmpty()) {
            throw new NotFoundException("У пользователя не найдена такая вещь.");
        }
        itemBookingDto = itemList
                .stream()
                .map(item -> mapToItemBookingDto(item, userId, item.getId()))
                .collect(Collectors.toList());
        return itemBookingDto;
    }

    @Override
    public ItemDto create(Long userId, ItemDto itemDto) {
        throwIfNotValid(itemDto);
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователя с идентификатором " + userId
                        + " нет в базе."));
        Item item = ItemMapper.toItem(itemDto);
        item.setOwner(owner);
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
    public ItemBookingDto getItemById(Long itemId, Long userId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Не найдена вещь с идентификатором " + itemId));
        return mapToItemBookingDto(item, userId, itemId);
    }

    @Override
    public List<ItemDto> searchItem(String text) {
        if (!text.isBlank()) {
            return itemRepository.searchItem(text).stream()
                    .map(ItemMapper::toItemDto)
                    .collect(Collectors.toList());
        }
        return List.of();
    }

    @Override
    public CommentDto createComment(Long userId, Long itemId, CommentDto commentDto) {
        if (getBookings(userId, itemId).isEmpty()) {
            throw new BadRequestException("Пользователь с ID = " + userId + " вещь с ID = "
                    + itemId + "не бронировал");
        }
        if (commentDto.getText() == null || commentDto.getText().isBlank()) {
            throw new BadRequestException("Текст комментария не может быть пустым");
        }
        Comment comment = CommentMapper.toComment(commentDto);
        comment.setId(comment.getId());
        comment.setText(comment.getText());
        comment.setItem(getItem(userId, itemId));
        comment.setAuthor(userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователя с идентификатором " + userId
                        + " нет в базе.")));
        comment.setCreated(LocalDateTime.now());
        return CommentMapper.toCommentDto(commentRepository.save(comment));
    }

    @Override
    public List<CommentDto> getCommentsByItemId(Long itemId) {
        return commentRepository.findAllByItemId(itemId).stream()
                .map(CommentMapper::toCommentDto)
                .collect(Collectors.toList());
    }

    private Item getItem(Long userId, Long itemId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователя с идентификатором " + userId
                        + " нет в базе."));
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
            if (bookings.size() == 1) {
                Booking booking = bookings.get(0);
                itemBookingDto.setLastBooking(ItemMapper.toBookingDateDto(booking));
                itemBookingDto.setNextBooking(ItemMapper.toBookingDateDto(new Booking()));
            } else {
                Booking min = bookings.get(0);
                Booking max = bookings.get(bookings.size() - 1);
                for (Booking booking : bookings) {
                    if (booking.getEnd().isBefore(LocalDateTime.now())) {
                        if (booking.getEnd().isAfter(min.getEnd())) {
                            min = booking;
                        }
                    }
                    if (booking.getStart().isAfter(LocalDateTime.now())) {
                        if (booking.getEnd().isBefore(max.getEnd())) {
                            max = booking;
                        }
                    }
                }
                itemBookingDto.setLastBooking(ItemMapper.toBookingDateDto(min));
                itemBookingDto.setNextBooking(ItemMapper.toBookingDateDto(max));
            }
        }
        itemBookingDto.setComments(getCommentsByItemId(item.getId()));
        return itemBookingDto;
    }

    private List<Booking> getBookings(Long userId, Long itemId) {
        return bookingRepository.findAllByItemIdAndBookerIdAndEndIsBeforeAndStatus(itemId,
                userId, LocalDateTime.now(), Status.APPROVED);
    }
}
