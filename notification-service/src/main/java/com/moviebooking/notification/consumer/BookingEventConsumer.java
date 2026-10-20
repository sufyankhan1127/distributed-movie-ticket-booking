
package com.moviebooking.notification.consumer;

import com.moviebooking.contracts.event.BookingEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class BookingEventConsumer {

    @KafkaListener(
            topics = "booking-events",
            groupId = "notification-service"
    )
    public void consumeBookingEvent(BookingEvent event) {
        System.out.println("========== BOOKING EVENT ==========");
        System.out.println("Booking ID: " + event.getBookingId());
        System.out.println("User ID: " + event.getUserId());
        System.out.println("Show ID: " + event.getShowId());
        System.out.println("Event Type: " + event.getEventType());
        System.out.println("Amount: " + event.getAmount());
        System.out.println("Occurred At: " + event.getOccurredAt());
        System.out.println("===================================");
    }
}
