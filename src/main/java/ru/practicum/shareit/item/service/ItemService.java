package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemBookingDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {

    List<ItemBookingDto> getItems(int from, int size, Long userId);

    ItemDto create(Long userId, ItemDto itemDto);

    ItemDto update(Long userId, Long itemId, ItemDto itemDto);

    ItemBookingDto getItemById(Long itemId, Long userId);

    List<ItemDto> searchItem(int from, int size, String text);

    CommentDto createComment(Long userId, Long itemId, CommentDto commentDto);

}
