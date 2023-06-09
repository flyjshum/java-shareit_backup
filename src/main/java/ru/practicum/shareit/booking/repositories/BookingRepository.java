package ru.practicum.shareit.booking.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingDate;
import ru.practicum.shareit.util.BookingStatus;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    @Query(nativeQuery=true, value = "SELECT * FROM bookings b WHERE b.booker_id = :userId " +
            "AND (:state = 'CURRENT' AND CURRENT_TIMESTAMP BETWEEN b.start_date AND b.end_date " +
            "OR :state = 'PAST' AND b.end_date < CURRENT_TIMESTAMP " +
            "OR :state = 'FUTURE' AND b.start_date > CURRENT_TIMESTAMP " +
            "OR :state = 'WAITING' AND b.status = 'WAITING' " +
            "OR :state = 'REJECTED' AND b.status = 'REJECTED' " +
            "OR :state = 'ALL') " +
            "ORDER BY b.start_date DESC")
    List<Booking> findAllUserBookingsByState(@Param("userId") Long userId, @Param("state") String state);
    // необязательное:
    // Можно было бы оставить возможность выбора сортировки, чтобы в случае,
    // когда нам потребуется другой порядок - не писать еще один метод)
    // Для этого в аргументах метода следует так же ожидать Sort sort.
    //Объект этого класса создается подобным образом: Sort.by(Sort.Direction.DESC, "start");
    //А ORDER BY b.start DESC следовательно нужно будет убрать
    //Аналогично с другими методами
    // - отмена

    @Query(nativeQuery=true, value = "SELECT * FROM bookings b JOIN items i ON b.item_id = i.id WHERE i.owner_id = :ownerId " +
            "AND (:state = 'CURRENT' AND CURRENT_TIMESTAMP BETWEEN b.start_date AND b.end_date " +
            "OR :state = 'PAST' AND b.end_date < CURRENT_TIMESTAMP " +
            "OR :state = 'FUTURE' AND b.start_date > CURRENT_TIMESTAMP " +
            "OR :state = 'WAITING' AND b.status = 'WAITING' " +
            "OR :state = 'REJECTED' AND b.status = 'REJECTED' " +
            "OR :state = 'ALL') " +
            "ORDER BY b.start_date DESC")
    List<Booking> findAllOwnerBookingsByState(@Param("ownerId") Long ownerId, @Param("state") String state);

    @Query(nativeQuery=true, value = "SELECT b.id, b.start_date AS bookingDate, b.booker_id AS bookerId " +
            "FROM bookings b WHERE b.item_id = ?1 AND b.start_date <= ?2 AND b.status = 'APPROVED'" +
            // <=, так как последнее бронирование может быть и текущим, если время старта равно текущему времени,
            // хоть эта ситуация практически невозможна
            // - done
            "ORDER BY b.start_date DESC LIMIT 1")
        // Бронирование должно быть именно в статусе APPROVED
        // - done
    BookingDate findLastBooking(Long itemId, LocalDateTime currentTime);

    @Query(nativeQuery=true, value = "SELECT b.id, b.start_date AS bookingDate, b.booker_id AS bookerId " +
            "FROM bookings b WHERE b.item_id = ?1 AND b.start_date > ?2 AND b.status = 'APPROVED'" +
            "ORDER BY b.start_date LIMIT 1")
        // Бронирование должно быть именно в статусе APPROVED
        // - done
    BookingDate findNextBooking(Long itemId, LocalDateTime currentTime);

    boolean existsBookingByBooker_IdAndItem_IdAndStatusAndStartBefore(Long userId, Long itemId, BookingStatus status, LocalDateTime startDate);

    @Query(nativeQuery=true, value = "SELECT id, b.item_id as itemId, b.start_date AS bookingDate, b.booker_id AS bookerId " +
            "FROM bookings b WHERE b.item_id IN (?1) AND b.start_date > ?2 AND b.status = 'APPROVED' " +
            "ORDER BY b.start_date")
        // Бронирование должно быть именно в статусе APPROVED
        // - done
    List<BookingDate> findAllNextBooking(List<Long> itemsId, LocalDateTime currentTime);

    @Query(nativeQuery=true, value = "SELECT id, b.item_id as itemId , b.start_date AS bookingDate, b.booker_id AS bookerId " +
            "FROM bookings b WHERE b.item_id IN (?1) AND b.end_date <= ?2 AND b.status = 'APPROVED' " +
            "ORDER BY b.start_date")
    List<BookingDate> findAllLastBooking(List<Long> itemsId, LocalDateTime currentTime);
}
