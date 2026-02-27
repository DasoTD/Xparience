package com.xparience.subscription;

import com.xparience.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionNotificationService {

    private final JavaMailSender mailSender;

    public void sendSubscriptionConfirmation(User user, Subscription subscription, String actionLabel) {
        if (user == null || user.getEmail() == null || user.getEmail().isBlank()) {
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(user.getEmail());
            message.setSubject("Xparience Subscription Update: " + actionLabel);

            BigDecimal total = subscription.getAmountPaid() == null ? BigDecimal.ZERO : subscription.getAmountPaid();
            String body = "Hi,\n\n"
                    + "Your subscription was updated successfully.\n"
                    + "Action: " + actionLabel + "\n"
                    + "Plan: " + subscription.getPlan().getDisplayName() + "\n"
                    + "Billing cycle: " + subscription.getBillingCycle() + "\n"
                    + "Total paid: £" + total + "\n"
                    + "Status: " + subscription.getStatus() + "\n\n"
                    + "Thank you for using Xparience.";
            message.setText(body);

            mailSender.send(message);
        } catch (Exception ex) {
            log.warn("Failed to send subscription confirmation email: {}", ex.getMessage());
        }
    }
}
