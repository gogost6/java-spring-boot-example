package com.example.demo.service;

import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.entity.CalendarPurchase;
import com.example.demo.entity.User;
import com.example.demo.exception.ForbiddenException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.AuthRepository;
import com.example.demo.repository.CalendarPurchaseRepository;

@Service
public class CalendarService {

    private static final Logger log = LoggerFactory.getLogger(CalendarService.class);

    private final CalendarPurchaseRepository purchaseRepository;
    private final AuthRepository authRepository;

    @Value("${calendar.pdf-path}")
    private String pdfPath;

    public CalendarService(CalendarPurchaseRepository purchaseRepository, AuthRepository authRepository) {
        this.purchaseRepository = purchaseRepository;
        this.authRepository = authRepository;
    }

    @Transactional
    public void fulfillPurchase(String email, String paymentIntentId) {
        User user = authRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (purchaseRepository.existsByUser(user)) {
            log.info("User {} already has calendar purchase, skipping duplicate for intent {}", email, paymentIntentId);
            return;
        }

        purchaseRepository.save(new CalendarPurchase(user, paymentIntentId));
        log.info("Calendar purchase fulfilled for user {} (intent: {})", email, paymentIntentId);
    }

    public boolean hasPurchased(String email) {
        User user = authRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return purchaseRepository.existsByUser(user);
    }

    public Resource downloadCalendar(String email) {
        if (!hasPurchased(email)) {
            throw new ForbiddenException("You have not purchased the calendar");
        }

        try {
            Resource resource;
            if (pdfPath.startsWith("classpath:")) {
                resource = new ClassPathResource(pdfPath.substring("classpath:".length()));
            } else {
                resource = new UrlResource(Paths.get(pdfPath).normalize().toUri());
            }
            if (!resource.exists() || !resource.isReadable()) {
                throw new ResourceNotFoundException("Calendar file not found");
            }
            return resource;
        } catch (ResourceNotFoundException | ForbiddenException e) {
            throw e;
        } catch (Exception e) {
            throw new ResourceNotFoundException("Calendar file not found");
        }
    }
}
