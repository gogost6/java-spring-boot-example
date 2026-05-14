package com.georgistoilkov.catify.controller;

import java.util.Map;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.georgistoilkov.catify.dto.CheckoutResponse;
import com.georgistoilkov.catify.service.CalendarService;
import com.georgistoilkov.catify.service.PaymentService;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;
    private final CalendarService calendarService;

    public PaymentController(PaymentService paymentService, CalendarService calendarService) {
        this.paymentService = paymentService;
        this.calendarService = calendarService;
    }

    @PostMapping("/checkout")
    public ResponseEntity<CheckoutResponse> checkout(@AuthenticationPrincipal Jwt jwt) throws Exception {
        return ResponseEntity.ok(paymentService.createPaymentIntent(jwt.getSubject()));
    }

    @GetMapping("/calendar/status")
    public ResponseEntity<Map<String, Boolean>> status(@AuthenticationPrincipal Jwt jwt) {
        boolean purchased = calendarService.hasPurchased(jwt.getSubject());
        return ResponseEntity.ok(Map.of("purchased", purchased));
    }

    @GetMapping("/calendar/download")
    public ResponseEntity<Resource> download(@AuthenticationPrincipal Jwt jwt) {
        Resource file = calendarService.downloadCalendar(jwt.getSubject());
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"cat-calendar.pdf\"")
                .body(file);
    }

    @PostMapping("/webhook")
    public ResponseEntity<Void> webhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) throws Exception {
        paymentService.handleWebhook(payload, sigHeader);
        return ResponseEntity.ok().build();
    }
}
