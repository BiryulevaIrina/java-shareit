package ru.practicum.shareit.item;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {
    List<Item> findByOwnerId(Long ownerId);

    @Query("select i from Item i " +
            "where i.available = true and " +
            "(lower(i.name) like lower(concat('%', ?1,'%')) or " +
            "lower(i.description) like lower(concat('%', ?1,'%')))")
    List<Item> searchItem(String text);
}