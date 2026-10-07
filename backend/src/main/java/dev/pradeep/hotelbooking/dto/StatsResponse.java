package dev.pradeep.hotelbooking.dto;

import java.math.BigDecimal;

public record StatsResponse(long totalRooms, long confirmedBookings, long cancelledBookings, BigDecimal revenue) {
}
