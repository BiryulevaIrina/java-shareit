package ru.practicum.shareit.request.service;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestMapper;
import ru.practicum.shareit.request.dto.ItemRequestSaveDto;
import ru.practicum.shareit.request.dto.ItemRequestWithResponsesDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.service.UserService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ItemRequestServiceImpl implements ItemRequestService {
    private final UserService userService;
    private final ItemRepository itemRepository;
    private final ItemRequestRepository itemRequestRepository;

    @Override
    public List<ItemRequestWithResponsesDto> getItemRequests(int from, int size, Long userId) {
        userService.getUserById(userId);
        Pageable pageable = PageRequest.of(from / size, size);

        List<ItemRequest> itemRequests = itemRequestRepository
                .findAllByRequesterIdNot(userId, pageable);
        List<ItemDto> itemDtos = itemRepository
                .findAllByRequestIsNotNull()
                .stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
        List<ItemRequestWithResponsesDto> requestsWithResponsesDtos = new ArrayList<>();
        itemRequests.stream().map(ItemRequestMapper::toItemRequestsWithResponsesDto)
                .forEach(itemRequestWithResponsesDto -> {
                    itemRequestWithResponsesDto.setItems(itemDtos);
                    requestsWithResponsesDtos.add(itemRequestWithResponsesDto);
                });
        return requestsWithResponsesDtos;
    }

    @Override
    public ItemRequestDto create(Long userId, ItemRequestSaveDto itemRequestDto) {
        userService.getUserById(userId);
        if (itemRequestDto.getDescription() == null || itemRequestDto.getDescription().isBlank()) {
            throw new BadRequestException("Отзыв не может быть пустым");
        }
        ItemRequest itemRequest = ItemRequestMapper.toItemRequest(itemRequestDto);
        itemRequest.setRequester(UserMapper.toUser(userService.getUserById(userId)));
        return ItemRequestMapper.toItemRequestDto(itemRequestRepository.save(itemRequest));
    }

    @Override
    public ItemRequestWithResponsesDto getItemRequestById(Long userId, Long requestId) {
        userService.getUserById(userId);
        ItemRequest itemRequest = itemRequestRepository
                .findById(requestId)
                .orElseThrow(() -> new NotFoundException("Запрос с ID = " + requestId + " не найден"));
        ItemRequestWithResponsesDto itemRequestWithResponsesDto = ItemRequestMapper
                .toItemRequestsWithResponsesDto(itemRequest);
        itemRequestWithResponsesDto.setItems(itemRepository
                .findAllByRequestId(itemRequest.getId())
                .stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList()));
        return itemRequestWithResponsesDto;
    }

    @Override
    public List<ItemRequestWithResponsesDto> getItemRequestsWithResponses(Long userId) {
        userService.getUserById(userId);
        List<ItemRequest> itemRequests = itemRequestRepository.findAllByRequesterId(userId);
        List<ItemRequestWithResponsesDto> requestsWithResponsesDtos = new ArrayList<>();
        itemRequests.forEach(itemRequest -> {
            ItemRequestWithResponsesDto itemRequestWithResponsesDto = ItemRequestMapper
                    .toItemRequestsWithResponsesDto(itemRequest);
            itemRequestWithResponsesDto.setItems(itemRepository.findAllByRequestId(itemRequest.getId())
                    .stream()
                    .map(ItemMapper::toItemDto)
                    .collect(Collectors.toList()));
            requestsWithResponsesDtos.add(itemRequestWithResponsesDto);
        });
        return requestsWithResponsesDtos;
    }
}
