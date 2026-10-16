
package com.moviebooking.payment.service;

import com.moviebooking.payment.dto.CreatePaymentRequest;
import com.moviebooking.payment.entity.Payment;
import com.moviebooking.payment.entity.PaymentStatus;
import com.moviebooking.payment.repository.PaymentRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;

    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    public Payment createPayment(CreatePaymentRequest request) {

        if (request.getBookingId() == null
                || request.getBookingId() <= 0) {
            throw new IllegalArgumentException(
                    "Valid booking ID is required");
        }

        BigDecimal amount = request.getAmount();

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Payment amount must be greater than zero");
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
