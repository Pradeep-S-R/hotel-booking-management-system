package dev.pradeep.hotelbooking.controller;

import dev.pradeep.hotelbooking.dto.BookingRequest;
import dev.pradeep.hotelbooking.dto.BookingResponse;
import dev.pradeep.hotelbooking.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public ResponseEntity<BookingResponse> create(Authentication authentication,
                                                  @Valid @RequestBody BookingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(bookingService.create(authentication.getName(), request));
    }

    @GetMapping("/my")
    public List<BookingResponse> mine(Authentication authentication) {
        return bookingService.myBookings(authentication.getName());
    }

    @PatchMapping("/{id}/cancel")
    public BookingResponse cancel(Authentication authentication, @PathVariable Long id) {
        return bookingService.cancel(id, authentication.getName(), false);
    }
}
