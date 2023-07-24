package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemBookingDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;

import java.util.List;

/**
 * TODO Sprint add-controllers.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/items")
@Slf4j
public class ItemController {
    private final ItemService itemService;

    @GetMapping
    public List<ItemBookingDto> getItems(@RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Получен запрос на просмотр владельцем с ID={} текущего списка своих вещей", userId);
        return itemService.getItems(userId);
    }

    @PostMapping
    public ItemDto createNewItem(@RequestHeader("X-Sharer-User-Id") Long userId,
                                 @RequestBody ItemDto itemDto) {
        log.info("Получен запрос на добавление вещи владельцем с ID={}", userId);
        return itemService.create(userId, itemDto);
    }

    @PatchMapping("/{itemId}")
    public ItemDto update(@RequestHeader("X-Sharer-User-Id") Long userId, @PathVariable Long itemId,
                          @RequestBody ItemDto itemDto) {
        log.info("Получен PUT-запрос на обновление вещи");
        return itemService.update(userId, itemId, itemDto);
    }

    @GetMapping("/{itemId}")
    public ItemBookingDto getItemById(@RequestHeader("X-Sharer-User-Id") Long userId, @PathVariable Long itemId) {
        log.info("Получен GET-запрос на вещь ID={} владельца с ID={}", itemId, userId);
        return itemService.getItemById(itemId, userId);
    }

    @GetMapping("/search")
    public List<ItemDto> searchItem(@RequestParam(value = "text") String text) {
        log.info("Получен GET-запрос на поиск вещи по тексту {}", text);
        return itemService.searchItem(text);
    }

    @PostMapping("/{itemId}/comment")
    public CommentDto createNewComment(@RequestHeader("X-Sharer-User-Id") Long userId, @PathVariable Long itemId,
                                       @RequestBody CommentDto commentDto) {
        log.info("Получен POST-запрос на добавление отзыва пользователем с ID={}", userId);
        return itemService.createComment(userId, itemId, commentDto);
    }
}
