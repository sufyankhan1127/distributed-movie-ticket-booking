
package com.moviebooking.booking.controller;

import com.moviebooking.booking.dto.CreateBookingRequest;
import com.moviebooking.booking.entity.Booking;
import com.moviebooking.booking.service.BookingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.moviebooking.booking.entity.BookingStatus;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public ResponseEntity<Booking> createBooking(
            @RequestBody CreateBookingRequest request) {

        Booking booking = bookingService.createBooking(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(booking);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Booking> updateBookingStatus(
            @PathVariable Long id,
            @RequestParam BookingStatus status) {

        return ResponseEntity.ok(
                bookingService.updateBookingStatus(id, status)
        );
    }

    @GetMapping
    public ResponseEntity<List<Booking>> getAllBookings() {
        return ResponseEntity.ok(bookingService.getAllBookings());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Booking> getBookingById(
            @PathVariable Long id) {

        return ResponseEntity.ok(bookingService.getBookingById(id));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Booking>> getBookingsByUserId(
            @PathVariable String userId) {

        return ResponseEntity.ok(
                bookingService.getBookingsByUserId(userId));
    }
}
