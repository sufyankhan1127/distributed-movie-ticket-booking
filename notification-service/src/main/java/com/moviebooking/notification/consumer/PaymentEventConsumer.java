
package com.moviebooking.notification.consumer;

import com.moviebooking.contracts.event.PaymentEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class PaymentEventConsumer {

    @KafkaListener(
            topics = "payment-events",
            groupId = "notification-service"
    )
    public void consumePaymentEvent(PaymentEvent event) {
        System.out.println("========== PAYMENT EVENT ==========");
        System.out.println("Payment ID: " + event.getPaymentId());
        System.out.println("Booking ID: " + event.getBookingId());
        System.out.println("Event Type: " + event.getEventType());
        System.out.println("Amount: " + event.getAmount());
        System.out.println("Occurred At: " + event.getOccurredAt());
        System.out.println("===================================");
    }
}
