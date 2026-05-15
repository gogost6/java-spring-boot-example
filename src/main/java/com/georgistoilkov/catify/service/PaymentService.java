package com.georgistoilkov.catify.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.georgistoilkov.catify.dto.CheckoutResponse;
import com.stripe.Stripe;
import com.stripe.exception.EventDataObjectDeserializationException;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;

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

    public CheckoutResponse createCheckoutSession(String email, String returnUrl) throws Exception {
        SessionCreateParams params = SessionCreateParams.builder()
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setPriceData(
                                        SessionCreateParams.LineItem.PriceData.builder()
                                                .setCurrency("usd")
                                                .setProductData(
                                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                .setName("Cat Calendar")
                                                                .build())
                                                .setUnitAmount(999L)
                                                .build())
                                .setQuantity(1L)
                                .build())
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setUiMode(SessionCreateParams.UiMode.EMBEDDED_PAGE)
                .setCustomerEmail(email)
                .setReturnUrl(returnUrl)
                .build();

        Session session = Session.create(params);
        return new CheckoutResponse(session.getClientSecret());
    }

    public void handleWebhook(String payload, String sigHeader) throws Exception {
        Event event;
        try {
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
        } catch (SignatureVerificationException e) {
            throw new IllegalArgumentException("Invalid Stripe webhook signature");
        }

        switch (event.getType()) {
            case "checkout.session.completed" -> {
                var deserializer = event.getDataObjectDeserializer();
                Session session = deserializer.getObject()
                        .map(o -> (Session) o)
                        .orElseGet(() -> {
                            try {
                                return (Session) deserializer.deserializeUnsafe();
                            } catch (EventDataObjectDeserializationException e) {
                                throw new RuntimeException(e);
                            }
                        });
                String email = session.getCustomerEmail();
                if (email != null) {
                    calendarService.fulfillPurchase(email, session.getPaymentIntent());
                } else {
                    log.warn("checkout.session.completed missing customer email: {}", session.getId());
                }
            }
            case "checkout.session.async_payment_failed" ->
                log.warn("Async payment failed for session: {}", event.getId());
            default -> log.debug("Unhandled Stripe event: {}", event.getType());
        }
    }
}
