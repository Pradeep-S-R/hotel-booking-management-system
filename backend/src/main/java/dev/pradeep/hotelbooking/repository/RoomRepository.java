package dev.pradeep.hotelbooking.repository;

import dev.pradeep.hotelbooking.model.Room;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface RoomRepository extends JpaRepository<Room, Long> {

    boolean existsByRoomNumber(String roomNumber);

    List<Room> findByActiveTrueOrderByPricePerNightAsc();

    /** Row lock so two people cannot book the same room at the same moment. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from Room r where r.id = :id")
    Optional<Room> findByIdForUpdate(@Param("id") Long id);

    @Query("""
            select r from Room r
            where r.active = true
              and r.capacity >= :guests
              and not exists (
                  select b.id from Booking b
                  where b.room = r
                    and b.status = dev.pradeep.hotelbooking.model.BookingStatus.CONFIRMED
                    and b.checkIn < :checkOut
                    and b.checkOut > :checkIn)
            order by r.pricePerNight asc
            """)
    List<Room> findAvailable(@Param("checkIn") LocalDate checkIn,
                             @Param("checkOut") LocalDate checkOut,
                             @Param("guests") int guests);
}
