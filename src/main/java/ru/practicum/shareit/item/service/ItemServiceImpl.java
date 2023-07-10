package ru.practicum.shareit.item.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemStorage;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserStorage;

import java.util.List;
import java.util.stream.Collectors;


import static java.util.stream.Collectors.toList;

@Service
public class ItemServiceImpl implements ItemService {
    ItemStorage itemStorage;
    UserStorage userStorage;

    @Autowired
    public ItemServiceImpl(ItemStorage itemStorage, UserStorage userStorage) {
        this.itemStorage = itemStorage;
        this.userStorage = userStorage;
    }

    @Override
    public List<ItemDto> getItems(Long userId) {
        userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователя с идентификатором " + userId
                        + " нет в базе."));
        return itemStorage.findAll().stream()
                .filter(item -> item.getOwner().getId().equals(userId))
                .map(ItemMapper::toItemDto)
                .collect(toList());
    }

    @Override
    public ItemDto create(Long userId, ItemDto itemDto) {
        User owner = userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователя с идентификатором " + userId
                        + " нет в базе."));
        throwIfNotValid(itemDto);
        throwIfNotAvailable(itemDto);
        Item item = ItemMapper.toItem(itemDto);
        item.setOwner(owner);
        return ItemMapper.toItemDto(itemStorage.create(item));
    }

    @Override
    public ItemDto update(Long userId, Long itemId, ItemDto itemDto) {
        userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователя с идентификатором " + userId
                        + " нет в базе."));
        getItemById(itemId);
        Item item1 = getItem(userId, itemId);
        if (!item1.getOwner().getId().equals(userId)) {
            throw new NotFoundException("У пользователя не найдена такая вещь.");
        }
        Item item = ItemMapper.toItem(itemDto);
        return ItemMapper.toItemDto(itemStorage.update(item, itemId));
    }

    @Override
    public ItemDto getItemById(Long itemId) {
        return ItemMapper.toItemDto(itemStorage.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь с идентификатором " + itemId
                        + " отсутствует в базе.")));
    }

    @Override
    public List<ItemDto> getItemsByText(String text) {
        if (!text.isBlank()) {
            String finalText = text.toLowerCase();
            return itemStorage.findAll().stream()
                    .map(ItemMapper::toItemDto)
                    .filter(ItemDto::getAvailable)
                    .filter(itemDto -> itemDto.getName().toLowerCase().contains(finalText) ||
                            itemDto.getDescription().toLowerCase().contains(finalText))
                    .collect(Collectors.toList());
        }
        return List.of();
    }

    private void throwIfNotValid(ItemDto itemDto) {
        if ((itemDto.getName() == null) || itemDto.getName().isBlank()) {
            throw new BadRequestException("Наименование вещи не может быть пустым");
        }
        if ((itemDto.getDescription() == null) || itemDto.getDescription().isBlank()) {
            throw new BadRequestException("Описание вещи не может быть пустым");
        }
    }

    private void throwIfNotAvailable(ItemDto itemDto) {
        if (itemDto.getAvailable() == null) {
            throw new BadRequestException("Статус доступности вещи должен быть установлен");
        }
    }

    private Item getItem(Long userId, Long itemId) {
        userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователя с идентификатором " + userId
                        + " нет в базе."));
        return itemStorage.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь с идентификатором " + itemId
                        + " отсутствует в базе."));
    }
}
