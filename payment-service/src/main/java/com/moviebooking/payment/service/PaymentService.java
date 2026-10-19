
package com.moviebooking.payment.service;

import com.moviebooking.contracts.event.PaymentEvent;
import com.moviebooking.payment.dto.CreatePaymentRequest;
import com.moviebooking.payment.entity.Payment;
import com.moviebooking.payment.entity.PaymentStatus;
import com.moviebooking.payment.event.PaymentEventProducer;
import com.moviebooking.payment.repository.PaymentRepository;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final RestTemplate restTemplate;
    private final PaymentEventProducer paymentEventProducer;

    public PaymentService(
            PaymentRepository paymentRepository,
            PaymentEventProducer paymentEventProducer) {

        this.paymentRepository = paymentRepository;
        this.paymentEventProducer = paymentEventProducer;
        this.restTemplate = new RestTemplate();
    }

    public Payment updatePaymentStatus(
            Long paymentId, PaymentStatus status) {

        if (status != PaymentStatus.SUCCESS
                && status != PaymentStatus.FAILED) {
            throw new IllegalArgumentException(
                    "Only SUCCESS or FAILED is allowed here");
        }

        Payment payment = getPaymentById(paymentId);

        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new IllegalArgumentException(
                    "Only pending payments can be updated");
        }

        payment.setStatus(status);
        Payment savedPayment = paymentRepository.save(payment);

        String bookingStatus =
                status == PaymentStatus.SUCCESS
                        ? "CONFIRMED"
                        : "FAILED";

        try {
            restTemplate.patchForObject(
                    "http://localhost:8084/api/bookings/{id}/status?status={status}",
                    null,
                    Map.class,
                    savedPayment.getBookingId(),
                    bookingStatus
            );
        } catch (RestClientException ex) {
            throw new IllegalStateException(
                    "Payment status saved, but booking status update failed",
                    ex
            );
        }

        PaymentEvent event = new PaymentEvent();
        event.setPaymentId(savedPayment.getId());
        event.setBookingId(savedPayment.getBookingId());
        event.setEventType(savedPayment.getStatus().name());
        event.setAmount(savedPayment.getAmount());
        event.setOccurredAt(LocalDateTime.now());

        paymentEventProducer.publishPaymentEvent(event);

        return savedPayment;
    }

    public Payment createPayment(CreatePaymentRequest request) {

        if (request.getBookingId() == null
                || request.getBookingId() <= 0) {
            throw new IllegalArgumentException(
                    "Valid booking ID is required");
        }

        BigDecimal amount = request.getAmount();

        if (amount == null
                || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Payment amount must be greater than zero");
        }

        Map<String, Object> booking;

        try {
            booking = restTemplate.exchange(
                    "http://localhost:8084/api/bookings/{id}",
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<Map<String, Object>>() {},
                    request.getBookingId()
            ).getBody();

        } catch (RestClientException ex) {
            throw new IllegalArgumentException(
                    "Could not retrieve booking. Verify the booking ID "
                            + "and ensure Booking Service is running.",
                    ex
            );
        }

        if (booking == null) {
            throw new IllegalArgumentException("Booking not found");
        }

        Object bookingAmount = booking.get("totalAmount");

        if (bookingAmount == null
                || new BigDecimal(bookingAmount.toString())
                .compareTo(amount) != 0) {
            throw new IllegalArgumentException(
                    "Payment amount does not match booking total");
        }

        List<Payment> existingPayments =
                paymentRepository.findByBookingId(
                        request.getBookingId());

        if (!existingPayments.isEmpty()) {
            throw new IllegalArgumentException(
                    "A payment record already exists for this booking");
        }

        Payment payment = new Payment();
        payment.setBookingId(request.getBookingId());
        payment.setAmount(amount);
        payment.setStatus(PaymentStatus.PENDING);

        return paymentRepository.save(payment);
    }

    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    public Payment getPaymentById(Long id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Payment not found with ID: " + id));
    }

    public List<Payment> getPaymentsByBookingId(Long bookingId) {
        return paymentRepository.findByBookingId(bookingId);
    }
}
