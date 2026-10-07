package dev.pradeep.hotelbooking.exception;

import java.time.LocalDateTime;

public record ApiError(LocalDateTime timestamp, int status, String message) {

    public static ApiError of(int status, String message) {
        return new ApiError(LocalDateTime.now(), status, message);
    }
}
