package dev.pradeep.hotelbooking.controller;

import dev.pradeep.hotelbooking.dto.RoomResponse;
import dev.pradeep.hotelbooking.service.RoomService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @GetMapping
    public List<RoomResponse> all() {
        return roomService.listActive();
    }

    @GetMapping("/available")
    public List<RoomResponse> available(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut,
            @RequestParam(defaultValue = "1") int guests) {
        return roomService.findAvailable(checkIn, checkOut, guests);
    }

    @GetMapping("/{id}")
    public RoomResponse one(@PathVariable Long id) {
        return roomService.getById(id);
    }
}
