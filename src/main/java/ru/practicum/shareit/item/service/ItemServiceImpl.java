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
        return itemStorage.findAll(userId).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public ItemDto create(Long userId, ItemDto itemDto) {
        User owner = userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователя с идентификатором " + userId
                        + " нет в базе."));
        throwIfNotValid(itemDto);
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
        return itemStorage.findById(itemId)
                .map(ItemMapper::toItemDto)
                .orElseThrow(() -> new NotFoundException("Не найдена вещь с идентификатором " + itemId));
    }

    @Override
    public List<ItemDto> searchItem(String text) {
        return itemStorage.searchItem(text);
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

    private Item getItem(Long userId, Long itemId) {
        userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователя с идентификатором " + userId
                        + " нет в базе."));
        return itemStorage.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь с идентификатором " + itemId
                        + " отсутствует в базе."));
    }
}
