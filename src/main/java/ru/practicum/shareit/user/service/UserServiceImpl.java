package ru.practicum.shareit.user.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.storage.UserStorage;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {
    public UserStorage userStorage;

    @Autowired
    public UserServiceImpl(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    @Override
    public List<UserDto> getUsers() {
        return userStorage.findAll().stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserDto create(UserDto userDto) throws BadRequestException, NotFoundException {
        throwIfEmailExist(userDto);
        throwIfNotValid(userDto);
        return UserMapper.toUserDto(userStorage.create(UserMapper.toUser(userDto)));
    }

    @Override
    public UserDto update(UserDto userDto, Long userId) {
        if (Objects.equals(userDto.getEmail(), getUserById(userId).getEmail())) {
            userDto.setId(userId);
            userDto.setName(getUserById(userId).getName());
            return userDto;
        } else {
            throwIfEmailExist(userDto);
            getUserById(userId);
            return UserMapper.toUserDto(userStorage.update(UserMapper.toUser(userDto), userId));
        }
    }

    @Override
    public UserDto getUserById(Long userId) {
        return userStorage.findById(userId)
                .map(UserMapper::toUserDto)
                .orElseThrow(() -> new NotFoundException("Пользователя с идентификатором " + userId + " нет в базе."));
    }

    @Override
    public UserDto delete(Long userId) {
        UserDto userDto = getUserById(userId);
        userStorage.delete(userId);
        return userDto;
    }

    private void throwIfNotValid(UserDto userDto) {
        if (userDto.getEmail() == null || userDto.getEmail().isBlank() || (!userDto.getEmail().contains("@"))) {
            throw new BadRequestException("Электронная почта не может быть пустой и должна содержать символ @");
        }
        if (userDto.getName().isBlank() || userDto.getName().contains(" ")) {
            throw new BadRequestException("Логин не может быть пустым и содержать пробелы");
        }
    }

    private void throwIfEmailExist(UserDto userDto) {
        if (getUsers().stream().anyMatch(u -> u.getEmail().equals(userDto.getEmail()))) {
            throw new ConflictException("Е-mail " + userDto.getEmail() + " уже существует");
        }
    }
}
