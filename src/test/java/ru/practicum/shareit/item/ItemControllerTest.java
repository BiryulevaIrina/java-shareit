package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingDateDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemBookingDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemServiceImpl;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemController.class)
public class ItemControllerTest {
    @Autowired
    ObjectMapper mapper;
    @MockBean
    private ItemServiceImpl itemService;
    @Autowired
    MockMvc mockMvc;

    private ItemBookingDto itemBookingDto;
    private User user;
    private Item item;
    private ItemDto itemDto;
    private CommentDto commentDto;
    private int from;
    private int size;
    private String text;


    @BeforeEach
    void setUp() {
        user = new User(1L, "user", "user@email.ru");
        item = new Item(1L, "item", "item description", true, user, new ItemRequest());
        itemDto = new ItemDto(1L, "item", "item description", true, 1L, 1L);
        commentDto = new CommentDto(1L, "commentDto", item, "userName", LocalDateTime.now());
        itemBookingDto = new ItemBookingDto(1L, "item", "Request", true,
                new BookingDateDto(1L, 1L), new BookingDateDto(2L, 2L), List.of());
        from = 1;
        size = 1;
        text = "text";
    }

    @Test
    void getItemsTest() throws Exception {
        when(itemService.getItems(1, 1, user.getId())).thenReturn(Collections.singletonList(itemBookingDto));

        mockMvc.perform(get("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", "1")
                        .param("from", "1")
                        .param("size", "1"))
                .andExpect(status().isOk());

        verify(itemService, Mockito.times(1)).getItems(from, size, user.getId());
    }

    @Test
    void createNewItemTest() throws Exception {
        when(itemService.create(1L, itemDto)).thenReturn(itemDto);

        mockMvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(itemDto))
                        .header("X-Sharer-User-Id", "1"))
                .andExpect(status().isOk());

        verify(itemService, Mockito.times(1)).create(user.getId(), itemDto);
    }

    @Test
    void updateItemTest() throws Exception {
        when(itemService.update(eq(1L), eq(1L), any())).thenReturn(itemDto);

        mockMvc.perform(patch("/items/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(itemDto))
                        .header("X-Sharer-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(itemDto.getName()), String.class))
                .andExpect(jsonPath("$.description", is(itemDto.getDescription()), String.class));
    }

    @Test
    void getItemById() throws Exception {
        when(itemService.create(1L, itemDto)).thenReturn(itemDto);
        when(itemService.getItemById(1L, 1L)).thenReturn(itemBookingDto);

        mockMvc.perform(get("/items/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemBookingDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(itemBookingDto.getName()), String.class))
                .andExpect(jsonPath("$.description", is(itemBookingDto.getDescription()), String.class));

        verify(itemService, Mockito.times(1)).getItemById(1L, 1L);
    }

    @Test
    void searchItem() throws Exception {
        when(itemService.searchItem(from, size, text)).thenReturn(Collections.singletonList(itemDto));

        mockMvc.perform(get("/items/search?text=test")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", "1"))
                .andExpect(status().isOk());

    }

    @Test
    void createNewComment() throws Exception {
        when(itemService.createComment(1L, 1L, commentDto)).thenReturn(commentDto);

        mockMvc.perform(post("/items/1/comment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(commentDto))
                        .header("X-Sharer-User-Id", "1"))
                .andExpect(status().isOk());

    }
}