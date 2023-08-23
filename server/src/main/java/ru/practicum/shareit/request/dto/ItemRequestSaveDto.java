package ru.practicum.shareit.request.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ItemRequestSaveDto {
    private Long id;
    private String description;
}
