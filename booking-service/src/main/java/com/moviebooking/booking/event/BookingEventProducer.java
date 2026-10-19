
package com.moviebooking.booking.event;

import com.moviebooking.contracts.event.BookingEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class BookingEventProducer {

    private static final String TOPIC = "booking-events";

    private final KafkaTemplate<String, BookingEvent> kafkaTemplate;

    public BookingEventProducer(
            KafkaTemplate<String, BookingEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishBookingEvent(BookingEvent event) {
        kafkaTemplate.send(
                TOPIC,
                String.valueOf(event.getBookingId()),
                event
        );
    }
}
