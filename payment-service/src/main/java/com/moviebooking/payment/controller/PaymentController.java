
package com.moviebooking.payment.controller;

import com.moviebooking.payment.dto.CreatePaymentRequest;
import com.moviebooking.payment.entity.Payment;
import com.moviebooking.payment.service.PaymentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.moviebooking.payment.entity.PaymentStatus;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }



    @PatchMapping("/{id}/status")
    public ResponseEntity<Payment> updatePaymentStatus(
            @PathVariable Long id,
            @RequestParam PaymentStatus status) {

        return ResponseEntity.ok(
                paymentService.updatePaymentStatus(id, status)
        );
    }


    @PostMapping
    public ResponseEntity<Payment> createPayment(
            @RequestBody CreatePaymentRequest request) {

        Payment payment = paymentService.createPayment(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(payment);
    }

    @GetMapping
    public ResponseEntity<List<Payment>> getAllPayments() {
        return ResponseEntity.ok(paymentService.getAllPayments());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Payment> getPaymentById(
            @PathVariable Long id) {

        return ResponseEntity.ok(paymentService.getPaymentById(id));
    }

    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<List<Payment>> getPaymentsByBookingId(
            @PathVariable Long bookingId) {

        return ResponseEntity.ok(
                paymentService.getPaymentsByBookingId(bookingId));
    }
}
