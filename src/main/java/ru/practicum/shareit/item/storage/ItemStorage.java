package ru.practicum.shareit.item.storage;

import ru.practicum.shareit.item.model.Item;

import java.util.List;
import java.util.Optional;

public interface ItemStorage {
    List<Item> findAll();

    Item create(Item item);

    Item update(Item item, Long itemId);

    Optional<Item> findById(Long id);

}
