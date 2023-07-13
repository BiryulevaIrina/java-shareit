package ru.practicum.shareit.item.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;

import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
public class InMemoryItemStorage implements ItemStorage {
    private final Map<Long, Item> items = new HashMap<>();
    private Long id = 0L;

    @Override
    public List<Item> findAll(Long userId) {
        List<Item> itemsList = items.values().stream()
                .filter(item -> item.getOwner().getId().equals(userId))
                .collect(Collectors.toList());
        log.debug("Текущее количество вещей пользователя с id {} - {}: ", userId, items.size());
        return itemsList;
    }

    @Override
    public Item create(Item item) {
        item.setId(++id);
        items.put(item.getId(), item);
        log.debug("Добавлена новая вещь" + item);
        return items.get(id);
    }

    @Override
    public Item update(Item item, Long itemId) {
        Item updateItem = items.get(itemId);
        if (item.getAvailable() != null) {
            updateItem.setAvailable(item.getAvailable());
        }
        if (item.getName() != null) {
            updateItem.setName(item.getName());
        }
        if (item.getDescription() != null) {
            updateItem.setDescription(item.getDescription());
        }
        log.debug("Обновлены данные о вещи" + item.getName() + " c id {} ", itemId);
        return updateItem;
    }

    @Override
    public Optional<Item> findById(Long id) {
        return Optional.ofNullable(items.get(id));
    }

    @Override
    public List<ItemDto> searchItem(String text) {
        if (!text.isBlank()) {
            String finalText = text.toLowerCase();
            return items.values().stream()
                    .map(ItemMapper::toItemDto)
                    .filter(ItemDto::getAvailable)
                    .filter(itemDto -> itemDto.getName().toLowerCase().contains(finalText) ||
                            itemDto.getDescription().toLowerCase().contains(finalText))
                    .collect(Collectors.toList());
        }
        return List.of();
    }
}
