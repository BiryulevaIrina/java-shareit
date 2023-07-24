package ru.practicum.shareit.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByBookerIdOrderByStartDesc(Long bookerId);

    List<Booking> findByBookerIdAndStartIsBeforeAndEndIsAfterOrderByStartDesc(Long bookerId, LocalDateTime start,
                                                                              LocalDateTime end);

    List<Booking> findByBookerIdAndEndIsBeforeOrderByStartDesc(Long bookerId, LocalDateTime end);

    List<Booking> findByBookerIdAndStartIsAfterOrderByStartDesc(Long bookerId, LocalDateTime start);

    List<Booking> findByBookerIdAndStatusOrderByStartDesc(Long bookerId, Status status);

    List<Booking> findByItemOwnerIdOrderByStartDesc(Long ownerId);

    List<Booking> findByItemOwnerIdAndStartIsBeforeAndEndIsAfterOrderByStartDesc(Long ownerId, LocalDateTime start,
                                                                                 LocalDateTime end);

    List<Booking> findByItemOwnerIdAndEndIsBeforeOrderByStartDesc(Long ownerId, LocalDateTime end);

    List<Booking> findByItemOwnerIdAndStartIsAfterOrderByStartDesc(Long ownerId, LocalDateTime start);

    List<Booking> findByItemOwnerIdAndStatusOrderByStartDesc(Long ownerId, Status status);

    List<Booking> findAllByItemIdAndStatusOrderByEndAsc(Long itemId, Status status);

    List<Booking> findAllByItemIdAndBookerIdAndEndIsBeforeAndStatus(Long itemId, Long userId,
                                                                    LocalDateTime end, Status status);
}
