package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestSaveDto;
import ru.practicum.shareit.request.dto.ItemRequestWithResponsesDto;

import java.util.List;

public interface ItemRequestService {

    List<ItemRequestWithResponsesDto> getItemRequests(int from, int size, Long userId);

    ItemRequestDto create(Long userId, ItemRequestSaveDto itemRequestDto);

    ItemRequestWithResponsesDto getItemRequestById(Long userId, Long requestId);

    List<ItemRequestWithResponsesDto> getItemRequestsWithResponses(Long userId);
}
