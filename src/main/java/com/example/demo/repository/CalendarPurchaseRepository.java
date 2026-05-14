package com.example.demo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.CalendarPurchase;
import com.example.demo.entity.User;

public interface CalendarPurchaseRepository extends JpaRepository<CalendarPurchase, Long> {
    boolean existsByUser(User user);

    Optional<CalendarPurchase> findByPaymentIntentId(String paymentIntentId);
}
