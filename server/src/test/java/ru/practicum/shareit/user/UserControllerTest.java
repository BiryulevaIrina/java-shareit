package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.Collections;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
public class UserControllerTest {
    @Autowired
    private ObjectMapper mapper;
    @MockBean
    private UserService userService;
    @Autowired
    private MockMvc mockMvc;
    private UserDto userDto, userDtoUpdate;

    @BeforeEach
    void setUp() {
        userDto = new UserDto(1L, "user", "user@mail.ru");
        userDtoUpdate = new UserDto(1L, "userUpdate", "userUpdate@mail.ru");
    }

    @Test
    void getUsersTest() throws Exception {
        when(userService.getUsers()).thenReturn(Collections.singletonList(userDto));

        MvcResult result = mockMvc.perform(get("/users")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        assertNotNull(result);
        verify(userService, Mockito.times(1)).getUsers();
    }

    @Test
    void createNewUserTest() throws Exception {
        when(userService.create(any())).thenReturn(userDto);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(userDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(userDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(userDto.getName()), String.class))
                .andExpect(jsonPath("$.email", is(userDto.getEmail()), String.class));

        verify(userService, Mockito.times(1)).create(userDto);
    }

    @Test
    void updateTest() throws Exception {
        when(userService.update(userDtoUpdate, 1L)).thenReturn(userDtoUpdate);

        mockMvc.perform(patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(userDtoUpdate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(userDtoUpdate.getName()), String.class))
                .andExpect(jsonPath("$.email", is(userDtoUpdate.getEmail()), String.class));

        verify(userService, Mockito.times(1)).update(userDtoUpdate, 1L);
    }

    @Test
    void getUserByIdTest() throws Exception {
        when(userService.getUserById(1L)).thenReturn(userDto);

        mockMvc.perform(get("/users/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(userDto.getName()), String.class))
                .andExpect(jsonPath("$.email", is(userDto.getEmail()), String.class));

        verify(userService, Mockito.times(1)).getUserById(1L);
    }

    @Test
    void getUserByIdNotTest() throws Exception {
        when(userService.getUserById(1L)).thenThrow(NotFoundException.class);

        mockMvc.perform(get("/users/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteUserTest() throws Exception {

        mockMvc.perform(delete("/users/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}