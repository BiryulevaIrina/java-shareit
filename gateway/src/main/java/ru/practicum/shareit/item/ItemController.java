package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@RestController
@RequiredArgsConstructor
@RequestMapping("/items")
@Slf4j
@Validated
public class ItemController {
    private final ItemClient itemClient;


    @GetMapping
    public ResponseEntity<Object> getItems(@RequestHeader("X-Sharer-User-Id") Long userId,
                                           @PositiveOrZero @RequestParam(defaultValue = "0") int from,
                                           @Positive @RequestParam(defaultValue = "10") int size) {
        log.info("Получен GET-запрос на просмотр владельцем с ID={} текущего списка своих вещей, from={}, size={}",
                userId, from, size);
        return itemClient.getItems(from, size, userId);
    }

    @PostMapping
    public ResponseEntity<Object> createNewItem(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                @Valid @RequestBody ItemDto itemDto) {
        log.info("Получен POST-запрос на добавление вещи владельцем с ID={}", userId);
        return itemClient.create(itemDto, userId);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> update(@RequestHeader("X-Sharer-User-Id") Long userId, @PathVariable Long itemId,
                                         @RequestBody ItemDto itemDto) {
        log.info("Получен PATCH-запрос на обновление вещи с ID={} пользователя с ID={}", itemId, userId);
        return itemClient.update(userId, itemId, itemDto);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> getItemById(@RequestHeader("X-Sharer-User-Id") Long userId,
                                              @PathVariable Long itemId) {
        log.info("Получен GET-запрос на вещь ID={} владельца с ID={}", itemId, userId);
        return itemClient.getItemById(userId, itemId);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> searchItem(@RequestHeader("X-Sharer-User-Id") Long userId,
                                             @RequestParam(value = "text") String text,
                                             @PositiveOrZero @RequestParam(defaultValue = "0") int from,
                                             @Positive @RequestParam(defaultValue = "10") int size) {
        log.info("Получен GET-запрос на поиск вещи по тексту {}, from={}, size={}", text, from, size);
        return itemClient.searchItem(userId, text, from, size);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> createNewComment(@RequestHeader("X-Sharer-User-Id") Long userId, @PathVariable Long itemId,
                                                   @RequestBody CommentDto commentDto) {
        log.info("Получен POST-запрос на добавление отзыва пользователем с ID={} о вещи с ID={} ", userId, itemId);
        return itemClient.createComment(userId, itemId, commentDto);
    }
}
