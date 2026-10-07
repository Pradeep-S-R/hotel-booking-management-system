package dev.pradeep.hotelbooking.dto;

import dev.pradeep.hotelbooking.model.RoomType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record RoomRequest(
        @NotBlank(message = "Room number is required") String roomNumber,
        @NotNull(message = "Room type is required") RoomType type,
        @NotNull(message = "Price is required") @DecimalMin(value = "1.0", message = "Price must be greater than zero") BigDecimal pricePerNight,
        @Min(value = 1, message = "Capacity must be at least 1") int capacity,
        String description,
        String amenities,
        boolean active) {
}
