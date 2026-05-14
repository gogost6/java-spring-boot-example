package com.example.demo.service;

import com.stripe.exception.EventDataObjectDeserializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.demo.dto.CheckoutResponse;
import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;
import com.stripe.param.PaymentIntentCreateParams;

import jakarta.annotation.PostConstruct;

@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final CalendarService calendarService;

    @Value("${stripe.secret-key}")
    private String secretKey;

    @Value("${stripe.webhook-secret}")
    private String webhookSecret;

    public PaymentService(CalendarService calendarService) {
        this.calendarService = calendarService;
    }

    @PostConstruct
    public void init() {
        Stripe.apiKey = secretKey;
    }

    public CheckoutResponse createPaymentIntent(String email) throws Exception {
        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(999L) // $9.99 in cents
                .setCurrency("usd")
                .setDescription("Cat Calendar")
                .putMetadata("email", email)
                .setAutomaticPaymentMethods(
                        PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                .setEnabled(true)
                                .build())
                .build();

        PaymentIntent intent = PaymentIntent.create(params);
        return new CheckoutResponse(intent.getClientSecret());
    }

    public void handleWebhook(String payload, String sigHeader) throws Exception {
        Event event;
        try {
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
        } catch (SignatureVerificationException e) {
            throw new IllegalArgumentException("Invalid Stripe webhook signature");
        }

        switch (event.getType()) {
            case "payment_intent.succeeded" -> {
                var deserializer = event.getDataObjectDeserializer();
                PaymentIntent intent = deserializer.getObject()
                        .map(o -> (PaymentIntent) o)
                        .orElseGet(() -> {
                            try {
                                return (PaymentIntent) deserializer.deserializeUnsafe();
                            } catch (EventDataObjectDeserializationException e) {
                                throw new RuntimeException(e);
                            }
                        });
                String email = intent.getMetadata().get("email");
                if (email != null) {
                    calendarService.fulfillPurchase(email, intent.getId());
                } else {
                    log.warn("payment_intent.succeeded missing email metadata: {}", intent.getId());
                }
            }
            case "payment_intent.payment_failed" -> log.warn("Payment failed: {}", event.getId());
            default -> log.debug("Unhandled Stripe event: {}", event.getType());
        }
    }
}
