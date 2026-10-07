package dev.pradeep.hotelbooking.repository;

import dev.pradeep.hotelbooking.model.Booking;
import dev.pradeep.hotelbooking.model.BookingStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query("""
            select count(b) from Booking b
            where b.room.id = :roomId
              and b.status = dev.pradeep.hotelbooking.model.BookingStatus.CONFIRMED
              and b.checkIn < :checkOut
              and b.checkOut > :checkIn
            """)
    long countOverlapping(@Param("roomId") Long roomId,
                          @Param("checkIn") LocalDate checkIn,
                          @Param("checkOut") LocalDate checkOut);

    @EntityGraph(attributePaths = {"room", "user"})
    List<Booking> findByUserEmailOrderByCreatedAtDesc(String email);

    @EntityGraph(attributePaths = {"room", "user"})
    List<Booking> findAllByOrderByCreatedAtDesc();

    @EntityGraph(attributePaths = {"room", "user"})
    Optional<Booking> findWithDetailsById(Long id);

    long countByStatus(BookingStatus status);

    boolean existsByRoomId(Long roomId);

    @Query("select coalesce(sum(b.totalPrice), 0) from Booking b where b.status = dev.pradeep.hotelbooking.model.BookingStatus.CONFIRMED")
    BigDecimal confirmedRevenue();
}
