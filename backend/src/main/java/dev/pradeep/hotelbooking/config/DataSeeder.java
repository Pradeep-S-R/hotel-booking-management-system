package dev.pradeep.hotelbooking.config;

import dev.pradeep.hotelbooking.model.Role;
import dev.pradeep.hotelbooking.model.Room;
import dev.pradeep.hotelbooking.model.RoomType;
import dev.pradeep.hotelbooking.model.User;
import dev.pradeep.hotelbooking.repository.RoomRepository;
import dev.pradeep.hotelbooking.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoomRepository roomRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.name}")
    private String adminName;
    @Value("${app.admin.email}")
    private String adminEmail;
    @Value("${app.admin.password}")
    private String adminPassword;

    public DataSeeder(UserRepository userRepository, RoomRepository roomRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roomRepository = roomRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (!userRepository.existsByEmail(adminEmail)) {
            userRepository.save(User.builder()
                    .fullName(adminName)
                    .email(adminEmail)
                    .password(passwordEncoder.encode(adminPassword))
                    .role(Role.ADMIN)
                    .build());
        }

        if (roomRepository.count() == 0) {
            roomRepository.saveAll(List.of(
                    room("101", RoomType.STANDARD, "3200", 2, "Queen bed, work desk and a quiet courtyard view.", "Wi-Fi,Air conditioning,Breakfast included"),
                    room("102", RoomType.STANDARD, "3200", 2, "Twin beds, ideal for colleagues travelling together.", "Wi-Fi,Air conditioning,Breakfast included"),
                    room("201", RoomType.DELUXE, "4800", 3, "King bed, seating area and a balcony facing the garden.", "Wi-Fi,Air conditioning,Balcony,Mini bar,Breakfast included"),
                    room("202", RoomType.DELUXE, "5100", 3, "Corner room with floor-to-ceiling windows and a rain shower.", "Wi-Fi,Air conditioning,Mini bar,Rain shower,Breakfast included"),
                    room("301", RoomType.FAMILY, "6400", 5, "Two bedrooms with a shared lounge, sleeps up to five.", "Wi-Fi,Air conditioning,Two bedrooms,Lounge,Breakfast included"),
                    room("302", RoomType.FAMILY, "6900", 6, "Spacious family room with a kitchenette and extra beds.", "Wi-Fi,Air conditioning,Kitchenette,Extra beds,Breakfast included"),
                    room("401", RoomType.SUITE, "9500", 3, "Top-floor suite with a separate living room and city views.", "Wi-Fi,Air conditioning,Living room,Bathtub,Mini bar,Airport pickup"),
                    room("402", RoomType.SUITE, "12500", 4, "Corner suite with a dining area, bathtub and private terrace.", "Wi-Fi,Air conditioning,Private terrace,Dining area,Bathtub,Airport pickup")
            ));
        }
    }

    private Room room(String number, RoomType type, String price, int capacity, String description, String amenities) {
        return Room.builder()
                .roomNumber(number)
                .type(type)
                .pricePerNight(new BigDecimal(price))
                .capacity(capacity)
                .description(description)
                .amenities(amenities)
                .active(true)
                .build();
    }
}
