package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestSaveDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.time.LocalDateTime;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(ItemRequestController.class)
public class ItemRequestControllerTest {
    @Autowired
    ObjectMapper mapper;
    @MockBean
    ItemRequestService itemRequestService;
    @Autowired
    MockMvc mockMvc;
    private ItemRequestSaveDto itemRequestSaveDto;
    Long userId;
    Long requestId;
    private int from;
    private int size;

    @BeforeEach
    void setUp() {
        userId = 1L;
        requestId = 1L;
        itemRequestSaveDto = new ItemRequestSaveDto(1L, "request description");
        from = 1;
        size = 1;
    }

    @Test
    void getItemRequests() throws Exception {
        mockMvc.perform(get("/requests/all")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("from", "1")
                        .param("size", "1")
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk());

        verify(itemRequestService, Mockito.times(1)).getItemRequests(from, size, userId);
    }

    @Test
    void createNewItemRequestTest() throws Exception {
        ItemRequestDto itemRequestDto = new ItemRequestDto(1L, "request description", LocalDateTime.now());

        mockMvc.perform(post("/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", userId)
                        .content(mapper.writeValueAsString(itemRequestDto)))
                .andExpect(status().isOk());

        verify(itemRequestService, Mockito.times(1)).create(userId, itemRequestSaveDto);
    }

    @Test
    void getItemRequestByIdTest() throws Exception {
        mockMvc.perform(get("/requests/{requestId}", requestId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk());

        verify(itemRequestService, Mockito.times(1)).getItemRequestById(userId, requestId);

    }

    @Test
    void getItemRequestsWithResponses() throws Exception {
        mockMvc.perform(get("/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk());

        verify(itemRequestService, Mockito.times(1)).getItemRequestsWithResponses(userId);
    }
}