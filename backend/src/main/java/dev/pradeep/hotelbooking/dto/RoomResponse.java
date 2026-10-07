package dev.pradeep.hotelbooking.dto;

import dev.pradeep.hotelbooking.model.Room;
import dev.pradeep.hotelbooking.model.RoomType;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

public record RoomResponse(
        Long id,
        String roomNumber,
        RoomType type,
        BigDecimal pricePerNight,
        int capacity,
        String description,
        List<String> amenities,
        boolean active) {

    public static RoomResponse from(Room room) {
        List<String> amenities = room.getAmenities() == null || room.getAmenities().isBlank()
                ? List.of()
                : Arrays.stream(room.getAmenities().split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList();
        return new RoomResponse(room.getId(), room.getRoomNumber(), room.getType(), room.getPricePerNight(),
                room.getCapacity(), room.getDescription(), amenities, room.isActive());
    }
}
