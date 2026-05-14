package com.georgistoilkov.catify.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.georgistoilkov.catify.entity.CalendarPurchase;
import com.georgistoilkov.catify.entity.User;

public interface CalendarPurchaseRepository extends JpaRepository<CalendarPurchase, Long> {
    boolean existsByUser(User user);

    Optional<CalendarPurchase> findByPaymentIntentId(String paymentIntentId);
}
