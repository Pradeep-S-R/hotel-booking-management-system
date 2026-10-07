package dev.pradeep.hotelbooking.service;

import dev.pradeep.hotelbooking.dto.BookingRequest;
import dev.pradeep.hotelbooking.dto.BookingResponse;
import dev.pradeep.hotelbooking.dto.StatsResponse;
import dev.pradeep.hotelbooking.exception.BadRequestException;
import dev.pradeep.hotelbooking.exception.BookingConflictException;
import dev.pradeep.hotelbooking.exception.ResourceNotFoundException;
import dev.pradeep.hotelbooking.model.Booking;
import dev.pradeep.hotelbooking.model.BookingStatus;
import dev.pradeep.hotelbooking.model.Room;
import dev.pradeep.hotelbooking.model.User;
import dev.pradeep.hotelbooking.repository.BookingRepository;
import dev.pradeep.hotelbooking.repository.RoomRepository;
import dev.pradeep.hotelbooking.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;

    public BookingService(BookingRepository bookingRepository,
                          RoomRepository roomRepository,
                          UserRepository userRepository) {
        this.bookingRepository = bookingRepository;
        this.roomRepository = roomRepository;
        this.userRepository = userRepository;
    }

    /**
     * Creates a booking. The room row is locked first, so concurrent requests for the
     * same room are processed one after another and an overlap is always detected.
     */
    @Transactional
    public BookingResponse create(String email, BookingRequest request) {
        if (request.checkIn().isBefore(LocalDate.now())) {
            throw new BadRequestException("Check-in cannot be in the past");
        }
        if (!request.checkOut().isAfter(request.checkIn())) {
            throw new BadRequestException("Check-out must be after check-in");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        Room room = roomRepository.findByIdForUpdate(request.roomId())
                .orElseThrow(() -> new ResourceNotFoundException("Room not found"));

        if (!room.isActive()) {
            throw new BadRequestException("This room is not available for booking");
        }
        if (request.guests() > room.getCapacity()) {
            throw new BadRequestException("Room " + room.getRoomNumber() + " sleeps up to " + room.getCapacity() + " guests");
        }
        if (bookingRepository.countOverlapping(room.getId(), request.checkIn(), request.checkOut()) > 0) {
            throw new BookingConflictException("Room " + room.getRoomNumber() + " is already booked for those dates");
        }

        long nights = ChronoUnit.DAYS.between(request.checkIn(), request.checkOut());
        BigDecimal total = room.getPricePerNight().multiply(BigDecimal.valueOf(nights));

        Booking booking = bookingRepository.save(Booking.builder()
                .user(user)
                .room(room)
                .checkIn(request.checkIn())
                .checkOut(request.checkOut())
                .guests(request.guests())
                .totalPrice(total)
                .status(BookingStatus.CONFIRMED)
                .createdAt(LocalDateTime.now())
                .build());

        return BookingResponse.from(booking);
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> myBookings(String email) {
        return bookingRepository.findByUserEmailOrderByCreatedAtDesc(email).stream()
                .map(BookingResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> allBookings() {
        return bookingRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(BookingResponse::from)
                .toList();
    }

    @Transactional
    public BookingResponse cancel(Long id, String email, boolean isAdmin) {
        Booking booking = bookingRepository.findWithDetailsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        if (!isAdmin && !booking.getUser().getEmail().equals(email)) {
            throw new AccessDeniedException("Not your booking");
        }
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new BadRequestException("This booking is already cancelled");
        }
        if (!isAdmin && !booking.getCheckIn().isAfter(LocalDate.now())) {
            throw new BadRequestException("Bookings can only be cancelled before the check-in date");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        return BookingResponse.from(bookingRepository.save(booking));
    }

    @Transactional(readOnly = true)
    public StatsResponse stats() {
        return new StatsResponse(
                roomRepository.count(),
                bookingRepository.countByStatus(BookingStatus.CONFIRMED),
                bookingRepository.countByStatus(BookingStatus.CANCELLED),
                bookingRepository.confirmedRevenue());
    }
}
