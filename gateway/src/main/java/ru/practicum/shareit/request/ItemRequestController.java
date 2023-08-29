package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestSaveDto;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/requests")
@Slf4j
@Validated
public class ItemRequestController {

    private final ItemRequestClient itemRequestClient;

    @GetMapping("/all")
    public ResponseEntity<Object> getItemRequests(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                  @PositiveOrZero @RequestParam(defaultValue = "0") int from,
                                                  @Positive @RequestParam(defaultValue = "10") int size) {
        log.info("Получен GET-запрос пользователя с ID={} на просмотр текущего списка запросов, from={}, size={}",
                userId, from, size);
        return itemRequestClient.getItemRequests(from, size, userId);
    }

    @PostMapping
    public ResponseEntity<Object> createNewItemRequest(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                       @Valid @RequestBody ItemRequestSaveDto itemRequestDto) {
        log.info("Получен POST-запрос на создание запроса вещи от пользователя с ID={}", userId);
        return itemRequestClient.create(userId, itemRequestDto);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> getItemRequestById(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                     @PathVariable Long requestId) {

        log.info("Получен GET-запрос на получение запроса с ID={} пользователя с ID={}", requestId, userId);
        return itemRequestClient.getItemRequestById(userId, requestId);
    }

    @GetMapping
    public ResponseEntity<Object> getItemRequestsWithResponses(@RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Получен GET-запрос на получение запросов пользователя с ID={}", userId);
        return itemRequestClient.getItemRequestsWithResponses(userId);
    }

}
