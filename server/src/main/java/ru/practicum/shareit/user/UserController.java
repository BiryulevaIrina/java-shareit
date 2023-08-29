package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

/**
 * TODO Sprint add-controllers.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/users")
@Slf4j
public class UserController {
    private final UserService userService;

    @GetMapping
    public List<UserDto> getUsers() {
        return userService.getUsers();
    }

    @PostMapping
    public UserDto createNewUser(@RequestBody UserDto userDto) {
        log.info("Получен POST-запрос на добавление пользователя");
        return userService.create(userDto);
    }

    @PatchMapping("/{id}")
    public UserDto update(@PathVariable("id") Long id, @RequestBody UserDto userDto)
            throws ConflictException {
        log.info("Получен PATCH-запрос на обновление пользователя с ID={}", id);
        return userService.update(userDto, id);
    }

    @GetMapping("/{id}")
    public UserDto getUserById(@PathVariable("id") Long id) {
        log.info("Получен GET-запрос на пользователя с ID={}", id);
        return userService.getUserById(id);
    }

    @DeleteMapping("/{id}")
    public UserDto delete(@PathVariable("id") Long id) {
        log.info("Получен DELETE-запрос на удаление пользователя с ID={}", id);
        return userService.delete(id);
    }
}
