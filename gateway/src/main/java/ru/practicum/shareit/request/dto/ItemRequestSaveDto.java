package ru.practicum.shareit.request.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
public class ItemRequestSaveDto {
    private Long id;
    @NotBlank
    private String description;
}
