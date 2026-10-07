package dev.pradeep.hotelbooking.controller;

import dev.pradeep.hotelbooking.dto.BookingResponse;
import dev.pradeep.hotelbooking.dto.RoomRequest;
import dev.pradeep.hotelbooking.dto.RoomResponse;
import dev.pradeep.hotelbooking.dto.StatsResponse;
import dev.pradeep.hotelbooking.service.BookingService;
import dev.pradeep.hotelbooking.service.RoomService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Everything under /api/admin requires the ADMIN role (see SecurityConfig). */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final RoomService roomService;
    private final BookingService bookingService;

    public AdminController(RoomService roomService, BookingService bookingService) {
        this.roomService = roomService;
        this.bookingService = bookingService;
    }

    @GetMapping("/stats")
    public StatsResponse stats() {
        return bookingService.stats();
    }

    @GetMapping("/rooms")
    public List<RoomResponse> rooms() {
        return roomService.listAll();
    }

    @PostMapping("/rooms")
    public ResponseEntity<RoomResponse> createRoom(@Valid @RequestBody RoomRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(roomService.create(request));
    }

    @PutMapping("/rooms/{id}")
    public RoomResponse updateRoom(@PathVariable Long id, @Valid @RequestBody RoomRequest request) {
        return roomService.update(id, request);
    }

    @DeleteMapping("/rooms/{id}")
    public ResponseEntity<Void> deleteRoom(@PathVariable Long id) {
        roomService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/bookings")
    public List<BookingResponse> bookings() {
        return bookingService.allBookings();
    }

    @PatchMapping("/bookings/{id}/cancel")
    public BookingResponse cancel(Authentication authentication, @PathVariable Long id) {
        return bookingService.cancel(id, authentication.getName(), true);
    }
}
