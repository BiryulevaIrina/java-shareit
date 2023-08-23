package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.exception.BadRequestException;
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
    public List<ItemBookingDto> getItems(@RequestHeader("X-Sharer-User-Id") Long userId,
                                         @RequestParam(defaultValue = "0") int from,
                                         @RequestParam(defaultValue = "10") int size) {
        log.info("Получен запрос на просмотр владельцем с ID={} текущего списка своих вещей", userId);
        if (from < 0 || size < 1) {
            throw new BadRequestException("Неправильно введен запрос (должно быть from >= 0, size > 0)");
        }
        return itemService.getItems(from, size, userId);
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
        return itemService.getItemById(userId, itemId);
    }

    @GetMapping("/search")
    public List<ItemDto> searchItem(@RequestParam(value = "text") String text,
                                    @RequestParam(defaultValue = "0") int from,
                                    @RequestParam(defaultValue = "10") int size) {
        log.info("Получен GET-запрос на поиск вещи по тексту {}", text);
        if (from < 0 || size < 1) {
            throw new BadRequestException("Неправильно введен запрос (должно быть from >= 0, size > 0)");
        }
        return itemService.searchItem(from, size, text);
    }

    @PostMapping("/{itemId}/comment")
    public CommentDto createNewComment(@RequestHeader("X-Sharer-User-Id") Long userId, @PathVariable Long itemId,
                                       @RequestBody CommentDto commentDto) {
        log.info("Получен POST-запрос на добавление отзыва пользователем с ID={}", userId);
        return itemService.createComment(userId, itemId, commentDto);
    }
}
