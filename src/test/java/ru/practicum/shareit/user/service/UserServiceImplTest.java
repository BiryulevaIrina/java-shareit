package ru.practicum.shareit.user.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {
    @InjectMocks
    private UserServiceImpl userService;
    @Mock
    private UserRepository userRepository;
    private User user;

    @BeforeEach
    void beforeEach() {
        user = new User(1L, "user", "user@email.ru");
    }

    @Test
    void getAllUsersTest() {
        when(userRepository.findAll()).thenReturn(Collections.singletonList(user));

        final List<UserDto> userDtos = userService.getUsers();

        assertNotNull(userDtos);
        assertEquals(1, userDtos.size());

        UserDto userDto = userDtos.get(0);

        assertEquals(userDto.getId(), user.getId());
        assertEquals(userDto.getName(), user.getName());
        assertEquals(userDto.getEmail(), user.getEmail());
    }

    @Test
    void createUserTest() {
        UserDto userToSave = new UserDto(1L, "user", "user@email.ru");
        when(userRepository.save(any())).thenReturn(user);

        UserDto userDto = userService.create(userToSave);

        assertEquals(user.getId(), userDto.getId());
        assertEquals(user.getName(), userDto.getName());
        assertEquals(user.getEmail(), userDto.getEmail());
    }

    @Test
    public void createUserNullNameTest() {
        UserDto userDto = new UserDto(1L, "", "user1@email.ru");
        Throwable thrown = catchThrowable(() -> userService.create(userDto));
        assertThat(thrown).isInstanceOf(BadRequestException.class);
        assertThat(thrown.getMessage()).isNotBlank();
        assertEquals("Логин не может быть пустым и содержать пробелы", thrown.getMessage());
    }

    @Test
    public void createUserEmailNotValidTest1() {
        UserDto userDto = new UserDto(1L, "user", "useremail.ru");
        Throwable thrown = catchThrowable(() -> userService.create(userDto));
        assertThat(thrown).isInstanceOf(BadRequestException.class);
        assertThat(thrown.getMessage()).isNotBlank();
        assertEquals("Электронная почта не может быть пустой и должна содержать символ @", thrown.getMessage());
    }

    @Test
    public void createUserEmailNotValidTest2() {
        UserDto userDto = new UserDto(1L, "user", " ");
        Throwable thrown = catchThrowable(() -> userService.create(userDto));
        assertThat(thrown).isInstanceOf(BadRequestException.class);
        assertThat(thrown.getMessage()).isNotBlank();
        assertEquals("Электронная почта не может быть пустой и должна содержать символ @", thrown.getMessage());
    }

    @Test
    void updateUserTest() {
        UserDto userDto = new UserDto(1L, "user", "mail@email.ru");
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        UserDto userDtoDb = userService.update(userDto, 1L);

        assertEquals(userDtoDb.getId(), userDto.getId());
        assertEquals(userDtoDb.getName(), userDto.getName());
        assertEquals(userDtoDb.getEmail(), userDto.getEmail());

        assertThrows(NotFoundException.class, () -> userService.update(userDto, 2L));
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void getUserByIdTest() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        UserDto userDto = userService.getUserById(1L);

        assertEquals(user.getId(), userDto.getId());
        assertEquals(user.getName(), userDto.getName());
        assertEquals(user.getEmail(), userDto.getEmail());

        final var thrown = assertThrows(NotFoundException.class, () -> userService.getUserById(2L));
        assertEquals("Пользователя с идентификатором 2 нет в базе.", thrown.getMessage());
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void deleteUserTest() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.ofNullable(user));

        userService.delete(user.getId());

        assertThrows(NotFoundException.class, () -> userService.delete(2L));

        final List<UserDto> userDtos = userService.getUsers();

        assertEquals(0, userDtos.size());
    }
}