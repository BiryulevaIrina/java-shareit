package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestSaveDto;
import ru.practicum.shareit.request.dto.ItemRequestWithResponsesDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.util.List;

/**
 * TODO Sprint add-item-requests.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/requests")
@Slf4j
public class ItemRequestController {

    private final ItemRequestService itemRequestService;

    @GetMapping("/all")
    public List<ItemRequestWithResponsesDto> getItemRequests(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                             @RequestParam(defaultValue = "0") int from,
                                                             @RequestParam(defaultValue = "10") int size) {
        log.info("Получен GET-запрос пользователя с ID={} на просмотр текущего списка запросов", userId);
        if (from < 0 || size < 1) {
            throw new BadRequestException("Неправильно введен запрос (должно быть from >= 0, size > 0)");
        }
        return itemRequestService.getItemRequests(from, size, userId);
    }

    @PostMapping
    public ItemRequestDto createNewItemRequest(@RequestHeader("X-Sharer-User-Id") Long userId,
                                               @RequestBody ItemRequestSaveDto itemRequestDto) {
        log.info("Получен POST-запрос на создание запроса вещи от пользователя с ID={}", userId);
        return itemRequestService.create(userId, itemRequestDto);
    }

    @GetMapping("/{requestId}")
    public ItemRequestWithResponsesDto getItemRequestById(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                          @PathVariable("requestId") Long requestId) {

        log.info("Получен GET-запрос на получение запроса с ID={} пользователя с ID={}", requestId, userId);
        return itemRequestService.getItemRequestById(userId, requestId);
    }

    @GetMapping
    public List<ItemRequestWithResponsesDto> getItemRequestsWithResponses(@RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Получен GET-запрос на на получение запросов пользователя с ID={}", userId);
        return itemRequestService.getItemRequestsWithResponses(userId);
    }

}
