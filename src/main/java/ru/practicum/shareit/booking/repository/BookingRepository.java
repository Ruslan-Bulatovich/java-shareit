package ru.practicum.shareit.booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findAllByBookerId(long bookerId, Sort sort);

    List<Booking> findAllByBookerIdAndStatus(long bookerId, Status status, Sort sort);

    List<Booking> findAllByBookerIdAndStartAfter(long bookerId, LocalDateTime localDateTime, Sort sort);

    List<Booking> findAllByBookerIdAndEndBefore(long bookerId, LocalDateTime localDateTime, Sort sort);

    @Query(value = "select b from Booking b where b.booker.id = ?1 and b.start < ?2 and b.end > ?2")
    List<Booking> findAllByBookerIdAndStartBeforeAndEndAfter(long bookerId, LocalDateTime localDateTime, Sort sort);

    @Query(value = "select b from Booking b where b.item.userId = ?1")
    List<Booking> findAllByOwnerId(long ownerId, Sort sort);

    @Query(value = "select b from Booking b where b.item.userId = ?1 and b.status = ?2")
    List<Booking> findAllByOwnerIdAndStatus(long ownerId, Status status, Sort sort);

    @Query(value = "select b from Booking b where b.item.userId = ?1 and b.start > ?2")
    List<Booking> findAllByOwnerIdAndStartAfter(long ownerId, LocalDateTime localDateTime, Sort sort);

    @Query(value = "select b from Booking b where b.item.userId = ?1 and b.end < ?2")
    List<Booking> findAllByOwnerIdAndEndBefore(long ownerId, LocalDateTime localDateTime, Sort sort);

    @Query(value = "select b from Booking b where b.item.userId = ?1 and b.start < ?2 and b.end > ?2")
    List<Booking> findAllByOwnerIdAndStartBeforeAndEndAfter(long bookerId, LocalDateTime localDateTime, Sort sort);

    List<Booking> findByItemId(long itemId, Sort sort);

    Optional<List<Booking>> findAllByItemIdAndBookerIdAndStatus(long itemId, long bookerId, Status status, Sort sort);
}
