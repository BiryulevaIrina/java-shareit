package ru.practicum.shareit.item.dto;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.model.Comment;

import javax.validation.constraints.NotNull;

@Component
public class CommentMapper {
    public static CommentDto toCommentDto(@NotNull Comment comment) {
        return new CommentDto(
                comment.getId(),
                comment.getText(),
                comment.getItem(),
                comment.getAuthor().getName(),
                comment.getCreated()
        );
    }

    public static Comment toComment(@NotNull CommentDto commentDto) {
        return new Comment(
                commentDto.getId(),
                commentDto.getText(),
                commentDto.getCreated()
        );
    }
}
