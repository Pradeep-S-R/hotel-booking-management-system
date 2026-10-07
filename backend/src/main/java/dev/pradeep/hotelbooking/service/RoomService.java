package dev.pradeep.hotelbooking.service;

import dev.pradeep.hotelbooking.dto.RoomRequest;
import dev.pradeep.hotelbooking.dto.RoomResponse;
import dev.pradeep.hotelbooking.exception.BadRequestException;
import dev.pradeep.hotelbooking.exception.ResourceNotFoundException;
import dev.pradeep.hotelbooking.model.Room;
import dev.pradeep.hotelbooking.repository.BookingRepository;
import dev.pradeep.hotelbooking.repository.RoomRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class RoomService {

    private final RoomRepository roomRepository;
    private final BookingRepository bookingRepository;

    public RoomService(RoomRepository roomRepository, BookingRepository bookingRepository) {
        this.roomRepository = roomRepository;
        this.bookingRepository = bookingRepository;
    }

    @Transactional(readOnly = true)
    public List<RoomResponse> listActive() {
        return roomRepository.findByActiveTrueOrderByPricePerNightAsc().stream().map(RoomResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<RoomResponse> listAll() {
        return roomRepository.findAll().stream()
                .sorted((a, b) -> a.getRoomNumber().compareTo(b.getRoomNumber()))
                .map(RoomResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RoomResponse> findAvailable(LocalDate checkIn, LocalDate checkOut, int guests) {
        validateDates(checkIn, checkOut);
        return roomRepository.findAvailable(checkIn, checkOut, Math.max(guests, 1)).stream()
                .map(RoomResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public RoomResponse getById(Long id) {
        return RoomResponse.from(find(id));
    }

    @Transactional
    public RoomResponse create(RoomRequest request) {
        if (roomRepository.existsByRoomNumber(request.roomNumber().trim())) {
            throw new BadRequestException("Room " + request.roomNumber() + " already exists");
        }
        Room room = new Room();
        apply(room, request);
        return RoomResponse.from(roomRepository.save(room));
    }

    @Transactional
    public RoomResponse update(Long id, RoomRequest request) {
        Room room = find(id);
        String number = request.roomNumber().trim();
        if (!room.getRoomNumber().equals(number) && roomRepository.existsByRoomNumber(number)) {
            throw new BadRequestException("Room " + number + " already exists");
        }
        apply(room, request);
        return RoomResponse.from(roomRepository.save(room));
    }

    @Transactional
    public void delete(Long id) {
        Room room = find(id);
        if (bookingRepository.existsByRoomId(id)) {
            throw new BadRequestException("This room has booking history. Mark it inactive instead of deleting it.");
        }
        roomRepository.delete(room);
    }

    private Room find(Long id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found"));
    }

    private void apply(Room room, RoomRequest request) {
        room.setRoomNumber(request.roomNumber().trim());
        room.setType(request.type());
        room.setPricePerNight(request.pricePerNight());
        room.setCapacity(request.capacity());
        room.setDescription(request.description());
        room.setAmenities(request.amenities());
        room.setActive(request.active());
    }

    private void validateDates(LocalDate checkIn, LocalDate checkOut) {
        if (checkIn.isBefore(LocalDate.now())) {
            throw new BadRequestException("Check-in cannot be in the past");
        }
        if (!checkOut.isAfter(checkIn)) {
            throw new BadRequestException("Check-out must be after check-in");
        }
    }
}
