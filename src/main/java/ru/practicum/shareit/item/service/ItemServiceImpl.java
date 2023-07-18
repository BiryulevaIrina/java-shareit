package ru.practicum.shareit.item.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.model.Booking;
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
        return itemRepository.findByOwnerId(userId).stream()
                .map(this::toItemDto)
                .collect(Collectors.toList());
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
    public ItemDto getItemById(Long itemId, Long userId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Не найдена вещь с идентификатором " + itemId));
        if (Objects.equals(item.getOwner().getId(), userId)) {
            return toItemDto(item);
        } else {
            return ItemMapper.toItemDto(item);
        }
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
        List<Booking> bookings = bookingRepository.findByItemIdAndBookerId(itemId, userId)
                .stream()
                .filter(booking -> booking.getStatus().toString().equals("APPROVED") &&
                        booking.getEnd().isBefore(LocalDateTime.now()))
                .collect(Collectors.toList());
        if (bookings.isEmpty()) {
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
        return commentRepository.findAllByItemIdOrderByCreatedDesc(itemId).stream()
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

    private ItemBookingDto toItemDto(Item item){
        ItemBookingDto itemBookingDto = new ItemBookingDto();
        itemBookingDto.setId(item.getId());
        itemBookingDto.setName(item.getName());
        itemBookingDto.setDescription(item.getDescription());
        itemBookingDto.setAvailable(item.getAvailable());

        itemBookingDto.setComments(getCommentsByItemId(item.getId()));
        return itemBookingDto;
    }

}
