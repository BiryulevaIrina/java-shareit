package ru.practicum.shareit.user.storage;

import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.Optional;

public interface UserStorage {
    List<User> findAll();

    User create(User user);

    User update(User user, Long userId) throws ConflictException;

    Optional<User> findById(Long id);

    void delete(Long userId);
}
