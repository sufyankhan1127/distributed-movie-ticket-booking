
package com.moviebooking.booking.service;

import com.moviebooking.booking.client.InventoryClient;
import com.moviebooking.booking.dto.CreateBookingRequest;
import com.moviebooking.booking.entity.Booking;
import com.moviebooking.booking.entity.BookingStatus;
import com.moviebooking.booking.event.BookingEventProducer;
import com.moviebooking.booking.repository.BookingRepository;
import com.moviebooking.contracts.event.BookingEvent;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final InventoryClient inventoryClient;
    private final BookingEventProducer bookingEventProducer;

    public BookingService(
            BookingRepository bookingRepository,
            InventoryClient inventoryClient,
            BookingEventProducer bookingEventProducer) {

        this.bookingRepository = bookingRepository;
        this.inventoryClient = inventoryClient;
        this.bookingEventProducer = bookingEventProducer;
    }

    public Booking updateBookingStatus(Long id, BookingStatus status) {

        Booking booking = getBookingById(id);
        booking.setStatus(status);

        return bookingRepository.save(booking);
    }

    public Booking createBooking(CreateBookingRequest request) {

        if (request.getUserId() == null
                || request.getUserId().isBlank()) {
            throw new IllegalArgumentException("User ID is required");
        }

        if (request.getShowId() == null
                || request.getShowId() <= 0) {
            throw new IllegalArgumentException(
                    "Valid show ID is required");
        }

        if (request.getNumberOfSeats() == null
                || request.getNumberOfSeats() <= 0) {
            throw new IllegalArgumentException(
                    "Number of seats must be greater than zero");
        }

        // 1. Reserve seats atomically through Inventory Service
        InventoryClient.InventoryDetails inventory =
                inventoryClient.reserveSeats(
                        request.getShowId(),
                        request.getNumberOfSeats()
                );

        // 2. Calculate total booking amount
        BigDecimal totalAmount = inventory.getTicketPrice()
                .multiply(
                        BigDecimal.valueOf(request.getNumberOfSeats())
                );

        // 3. Create and save the booking
        Booking booking = new Booking();
        booking.setUserId(request.getUserId());
        booking.setShowId(request.getShowId());
        booking.setNumberOfSeats(request.getNumberOfSeats());
        booking.setTotalAmount(totalAmount);
        booking.setStatus(BookingStatus.PENDING);

        Booking savedBooking = bookingRepository.save(booking);

        // 4. Publish the booking-created event to Kafka
        BookingEvent event = new BookingEvent();
        event.setBookingId(savedBooking.getId());
        event.setUserId(savedBooking.getUserId());
        event.setShowId(savedBooking.getShowId());
        event.setEventType("BOOKING_CREATED");
        event.setAmount(savedBooking.getTotalAmount());
        event.setOccurredAt(LocalDateTime.now());

        bookingEventProducer.publishBookingEvent(event);

        return savedBooking;
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
