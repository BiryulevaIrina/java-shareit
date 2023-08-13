package ru.practicum.shareit.request.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestSaveDto;
import ru.practicum.shareit.request.dto.ItemRequestWithResponsesDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ItemRequestServiceImplTest {
    @InjectMocks
    private ItemRequestServiceImpl itemRequestService;
    @Mock
    private ItemRequestRepository itemRequestRepository;
    @Mock
    private UserService userService;
    @Mock
    private ItemRepository itemRepository;
    private ItemRequest itemRequest;
    private ItemRequestSaveDto itemRequestSaveDto;

    private Item item;
    private UserDto userDto;
    private int from;
    private int size;

    @BeforeEach
    void setUp() {

        userDto = new UserDto(1L, "user", "user@email.ru");
        itemRequest = new ItemRequest(1L, "request description", LocalDateTime.now(),
                UserMapper.toUser(userDto));
        itemRequestSaveDto = new ItemRequestSaveDto(1L, "request description");
        item = new Item(1L, "item", "item description", true,
                UserMapper.toUser(userDto), itemRequest);
        from = 1;
        size = 1;
    }

    @Test
    void getItemRequestsTest() {
        User otherUser = new User(2L, "otherUser", "user@email.ru");
        Item otherItem = new Item(1L, "item", "item test", true, otherUser, itemRequest);

        when(itemRequestRepository
                .findAll(PageRequest.of(from / size, size)))
                .thenReturn(new PageImpl<>(List.of(itemRequest)));
        when(itemRepository
                .findAll())
                .thenReturn(Collections
                        .singletonList(otherItem));

        List<ItemRequestWithResponsesDto> itemRequests = itemRequestService.getItemRequests(from, size, 2L);

        assertNotNull(itemRequests);
        assertEquals(1, itemRequests.size());

        ItemRequestWithResponsesDto itemRequestWithResponsesDto = itemRequests.get(0);

        assertNotNull(itemRequestWithResponsesDto.getItems());
        assertEquals(itemRequest.getId(), itemRequestWithResponsesDto.getId());
        assertEquals(itemRequest.getDescription(), itemRequestWithResponsesDto.getDescription());
        assertEquals(itemRequest.getCreated().format(DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss")),
                itemRequestWithResponsesDto.getCreated().format(DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss")));

        assertNotNull(itemRequestWithResponsesDto.getItems());
        assertEquals(1, itemRequestWithResponsesDto.getItems().size());

        size = 0;
        assertThrows(BadRequestException.class, () -> itemRequestService.getItemRequests(from, size, 2L));
    }

    @Test
    void createItemRequestsTest() {
        when(userService.getUserById(userDto.getId())).thenReturn(userDto);
        when(itemRequestRepository.save(any())).thenReturn(itemRequest);

        ItemRequestDto itemRequestDto = itemRequestService.create(1L, itemRequestSaveDto);

        assertEquals(itemRequestSaveDto.getId(), itemRequestDto.getId());
        assertEquals(itemRequestSaveDto.getDescription(), itemRequestDto.getDescription());
        assertEquals(LocalDateTime.now().format((DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss"))),
                itemRequestDto.getCreated().format((DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss"))));
    }

    @Test
    public void createItemRequestsNullNameTest() {
        ItemRequestSaveDto itemRequestSaveDto1 = new ItemRequestSaveDto(1L, " ");
        Throwable thrown = catchThrowable(() -> {
            itemRequestService.create(1L, itemRequestSaveDto1);
        });
        assertThat(thrown).isInstanceOf(BadRequestException.class);
        assertThat(thrown.getMessage()).isNotBlank();
        assertEquals("Отзыв не может быть пустым", thrown.getMessage());
    }

    @Test
    void getItemRequestByIdTest() {
        when(itemRequestRepository.findById(itemRequest.getId())).thenReturn(Optional.of(itemRequest));

        ItemRequestWithResponsesDto itemRequestWithResponsesDto = itemRequestService
                .getItemRequestById(userDto.getId(), (itemRequest.getId()));

        assertEquals(itemRequest.getId(), itemRequestWithResponsesDto.getId());
        assertEquals(itemRequest.getDescription(), itemRequestWithResponsesDto.getDescription());
        assertEquals(itemRequest.getCreated().format(DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss")),
                itemRequestWithResponsesDto.getCreated().format(DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss")));
    }

    @Test
    void getItemRequestsWithResponses() {
        when(itemRequestRepository
                .findAllByRequesterId(userDto.getId()))
                .thenReturn(Collections.singletonList(itemRequest));
        when(itemRepository
                .findAllByRequestId(userDto.getId()))
                .thenReturn(Collections.singletonList(item));

        List<ItemRequestWithResponsesDto> itemRequests = itemRequestService
                .getItemRequestsWithResponses(userDto.getId());

        assertNotNull(itemRequests);
        assertEquals(1, itemRequests.size());

        ItemRequestWithResponsesDto itemRequestWithResponsesDto = itemRequests.get(0);

        assertEquals(itemRequest.getId(), itemRequestWithResponsesDto.getId());
        assertEquals(itemRequest.getDescription(), itemRequestWithResponsesDto.getDescription());
        assertEquals(itemRequest.getCreated().format(DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss")),
                itemRequestWithResponsesDto.getCreated().format(DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss")));

        assertNotNull(itemRequestWithResponsesDto.getItems());
        assertEquals(1L, itemRequestWithResponsesDto.getItems().size());
    }
}