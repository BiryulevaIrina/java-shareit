package ru.practicum.shareit.user.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.user.model.User;

import java.util.*;

@Component
@Slf4j
public class InMemoryUserStorage implements UserStorage {
    private final Map<Long, User> users = new HashMap<>();
    private Long id = 0L;

    @Override
    public List<User> findAll() {
        log.debug("Текущее количество пользователей: {}", users.size());
        return new ArrayList<>(users.values());
    }

    @Override
    public User create(User user) throws ConflictException {
        user.setId(++id);
        users.put(user.getId(), user);
        log.debug("Зарегистрирован новый пользователь" + user.getName() + user.getEmail());
        return user;
    }

    @Override
    public User update(User user, Long userId) {
        User updateUser = users.get(userId);
        if (user.getName() != null) {
            updateUser.setName(user.getName());
        }
        if (user.getEmail() != null) {
            updateUser.setEmail(user.getEmail());
        }
        log.debug("Обновлены данные о пользователе" + user.getName() + user.getEmail());
        return updateUser;
    }


    @Override
    public Optional<User> findById(Long id) {
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public void delete(Long userId) {
        users.remove(userId);
    }

}

