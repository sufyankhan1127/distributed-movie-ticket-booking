package com.moviebooking.contracts.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class BookingEvent {

    private String bookingId;
    private String userId;
    private String showId;
    private String eventType;
    private BigDecimal amount;
    private LocalDateTime occurredAt;

    public BookingEvent() {
    }

    public BookingEvent(
            String bookingId,
            String userId,
            String showId,
            String eventType,
            BigDecimal amount,
            LocalDateTime occurredAt) {
        this.bookingId = bookingId;
        this.userId = userId;
        this.showId = showId;
        this.eventType = eventType;
        this.amount = amount;
        this.occurredAt = occurredAt;
    }

    public String getBookingId() {
        return bookingId;
    }

    public void setBookingId(String bookingId) {
        this.bookingId = bookingId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getShowId() {
        return showId;
    }

    public void setShowId(String showId) {
        this.showId = showId;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(LocalDateTime occurredAt) {
        this.occurredAt = occurredAt;
    }
}
