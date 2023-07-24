package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import ru.practicum.shareit.item.model.Item;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class CommentDto {
    private long id;
    @NotBlank
    private String text;
    private Item item;
    private String authorName;
    private LocalDateTime created;

}
