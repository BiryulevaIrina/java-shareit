package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

public interface UserService {

    List<UserDto> getUsers();

    UserDto create(UserDto userDto);

    UserDto update(UserDto userDto, Long userId);

    UserDto getUserById(Long userId);

    UserDto delete(Long userId);


}
