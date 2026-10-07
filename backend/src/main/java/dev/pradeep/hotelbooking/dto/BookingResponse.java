package dev.pradeep.hotelbooking.dto;

import dev.pradeep.hotelbooking.model.Booking;
import dev.pradeep.hotelbooking.model.BookingStatus;
import dev.pradeep.hotelbooking.model.RoomType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public record BookingResponse(
        Long id,
        Long roomId,
        String roomNumber,
        RoomType roomType,
        String guestName,
        String guestEmail,
        LocalDate checkIn,
        LocalDate checkOut,
        long nights,
        int guests,
        BigDecimal totalPrice,
        BookingStatus status,
        LocalDateTime createdAt) {

    public static BookingResponse from(Booking b) {
        return new BookingResponse(
                b.getId(),
                b.getRoom().getId(),
                b.getRoom().getRoomNumber(),
                b.getRoom().getType(),
                b.getUser().getFullName(),
                b.getUser().getEmail(),
                b.getCheckIn(),
                b.getCheckOut(),
                ChronoUnit.DAYS.between(b.getCheckIn(), b.getCheckOut()),
                b.getGuests(),
                b.getTotalPrice(),
                b.getStatus(),
                b.getCreatedAt());
    }
}
