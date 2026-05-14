package com.georgistoilkov.catify.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "calendar_purchases")
public class CalendarPurchase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "payment_intent_id", nullable = false, unique = true)
    private String paymentIntentId;

    @CreationTimestamp
    @Column(name = "purchased_at", nullable = false, updatable = false)
    private LocalDateTime purchasedAt;

    public CalendarPurchase() {
    }

    public CalendarPurchase(User user, String paymentIntentId) {
        this.user = user;
        this.paymentIntentId = paymentIntentId;
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public String getPaymentIntentId() {
        return paymentIntentId;
    }

    public LocalDateTime getPurchasedAt() {
        return purchasedAt;
    }
}
