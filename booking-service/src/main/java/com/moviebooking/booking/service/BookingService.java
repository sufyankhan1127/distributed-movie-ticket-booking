
package com.moviebooking.booking.service;

import com.moviebooking.booking.client.InventoryClient;
import com.moviebooking.booking.dto.CreateBookingRequest;
import com.moviebooking.booking.entity.Booking;
import com.moviebooking.booking.entity.BookingStatus;
import com.moviebooking.booking.repository.BookingRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final InventoryClient inventoryClient;

    public BookingService(
            BookingRepository bookingRepository,
            InventoryClient inventoryClient) {
        this.bookingRepository = bookingRepository;
        this.inventoryClient = inventoryClient;
    }

    public Booking createBooking(CreateBookingRequest request) {

        if (request.getUserId() == null
                || request.getUserId().isBlank()) {
            throw new IllegalArgumentException("User ID is required");
        }

        if (request.getShowId() == null || request.getShowId() <= 0) {
            throw new IllegalArgumentException("Valid show ID is required");
        }

        if (request.getNumberOfSeats() == null
                || request.getNumberOfSeats() <= 0) {
            throw new IllegalArgumentException(
                    "Number of seats must be greater than zero");
        }

        InventoryClient.InventoryDetails inventory =
                inventoryClient.getInventory(request.getShowId());

        if (inventory.getAvailableSeats() < request.getNumberOfSeats()) {
            throw new IllegalArgumentException(
                    "Not enough seats available");
        }

        BigDecimal totalAmount = inventory.getTicketPrice()
                .multiply(BigDecimal.valueOf(request.getNumberOfSeats()));

        Booking booking = new Booking();
        booking.setUserId(request.getUserId());
        booking.setShowId(request.getShowId());
        booking.setNumberOfSeats(request.getNumberOfSeats());
        booking.setTotalAmount(totalAmount);
        booking.setStatus(BookingStatus.PENDING);

        return bookingRepository.save(booking);
    }

    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    public Booking getBookingById(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Booking not found with ID: " + id));
    }

    public List<Booking> getBookingsByUserId(String userId) {
        return bookingRepository.findByUserId(userId);
    }
}
