package com.xparience.subscription;

import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.Invoice;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;
import com.xparience.subscription.dto.CheckoutSessionResponse;
import com.xparience.subscription.dto.StripeWebhookResponse;
import com.xparience.subscription.dto.UpgradePlanRequest;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class StripeService {

    private final StripeProperties stripeProperties;
    private final SubscriptionService subscriptionService;
    private final StripeWebhookEventRepository stripeWebhookEventRepository;

    @PostConstruct
    void init() {
        if (stripeProperties.secretKey() != null && !stripeProperties.secretKey().isBlank()) {
            Stripe.apiKey = stripeProperties.secretKey();
        }
    }

    public CheckoutSessionResponse createCheckoutSession(Long userId, UpgradePlanRequest request) {
        ensureStripeSecretConfigured();

        try {
            long amountInPence = toPence(request.getPlan().getPrice(request.getBillingCycle()));

            SessionCreateParams.LineItem.PriceData.Recurring.Interval interval = SessionCreateParams.LineItem.PriceData.Recurring.Interval.MONTH;
            long intervalCount = request.getBillingCycle().getMonths();

            SessionCreateParams params = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.SUBSCRIPTION)
                    .setSuccessUrl(resolveSuccessUrl())
                    .setCancelUrl(resolveCancelUrl())
                    .putMetadata("userId", String.valueOf(userId))
                    .putMetadata("plan", request.getPlan().name())
                    .putMetadata("billingCycle", request.getBillingCycle().name())
                    .putMetadata("paymentMethod", request.getPaymentMethod().name())
                    .putMetadata("promoCode", request.getPromoCode() == null ? "" : request.getPromoCode())
                    .putMetadata("useTrial", String.valueOf(request.isUseTrial()))
                    .addLineItem(SessionCreateParams.LineItem.builder()
                            .setQuantity(1L)
                            .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                                    .setCurrency("gbp")
                                    .setUnitAmount(amountInPence)
                                    .setRecurring(SessionCreateParams.LineItem.PriceData.Recurring.builder()
                                            .setInterval(interval)
                                            .setIntervalCount(intervalCount)
                                            .build())
                                    .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                            .setName("Xparience " + request.getPlan().getDisplayName() + " Plan")
                                            .setDescription("Billing cycle: " + request.getBillingCycle().name())
                                            .build())
                                    .build())
                            .build())
                    .build();

            Session session = Session.create(params);

            CheckoutSessionResponse response = new CheckoutSessionResponse();
            response.setSessionId(session.getId());
            response.setCheckoutUrl(session.getUrl());
            return response;
        } catch (StripeException ex) {
            throw new RuntimeException("Failed to create Stripe checkout session: " + ex.getMessage(), ex);
        }
    }

    @Transactional
    public StripeWebhookResponse handleWebhook(String payload, String signatureHeader) {
        ensureStripeSecretConfigured();
        if (stripeProperties.webhookSecret() == null || stripeProperties.webhookSecret().isBlank()) {
            throw new RuntimeException("Stripe webhook secret is not configured");
        }

        Event event;
        try {
            event = Webhook.constructEvent(payload, signatureHeader, stripeProperties.webhookSecret());
        } catch (SignatureVerificationException ex) {
            throw new RuntimeException("Invalid Stripe webhook signature", ex);
        }

        StripeWebhookEvent webhookEvent = stripeWebhookEventRepository.findByEventId(event.getId())
                .orElseGet(() -> {
                    StripeWebhookEvent created = new StripeWebhookEvent();
                    created.setEventId(event.getId());
                    created.setEventType(event.getType());
                    created.setStatus(StripeWebhookEventStatus.PROCESSING);
                    created.setPayloadHash(hashPayload(payload));
                    created.setReceivedAt(LocalDateTime.now());
                    return created;
                });

        if (webhookEvent.getStatus() == StripeWebhookEventStatus.PROCESSED) {
            StripeWebhookResponse duplicateResponse = new StripeWebhookResponse();
            duplicateResponse.setEventType(event.getType());
            duplicateResponse.setStatus("duplicate_ignored");
            return duplicateResponse;
        }

        webhookEvent.setEventType(event.getType());
        webhookEvent.setStatus(StripeWebhookEventStatus.PROCESSING);
        webhookEvent.setPayloadHash(hashPayload(payload));
        webhookEvent.setErrorMessage(null);
        stripeWebhookEventRepository.save(webhookEvent);

        try {
            switch (event.getType()) {
                case "checkout.session.completed" -> {
                    Session session = (Session) event.getDataObjectDeserializer()
                            .getObject()
                            .orElseThrow(() -> new RuntimeException("Unable to parse checkout.session.completed payload"));
                    subscriptionService.processStripeCheckoutCompleted(session);
                }
                case "invoice.payment_succeeded" -> {
                    Invoice invoice = (Invoice) event.getDataObjectDeserializer()
                            .getObject()
                            .orElseThrow(() -> new RuntimeException("Unable to parse invoice.payment_succeeded payload"));
                    subscriptionService.processStripeInvoicePayment(invoice, true);
                }
                case "invoice.payment_failed" -> {
                    Invoice invoice = (Invoice) event.getDataObjectDeserializer()
                            .getObject()
                            .orElseThrow(() -> new RuntimeException("Unable to parse invoice.payment_failed payload"));
                    subscriptionService.processStripeInvoicePayment(invoice, false);
                }
                default -> {
                }
            }

            webhookEvent.setStatus(StripeWebhookEventStatus.PROCESSED);
            webhookEvent.setProcessedAt(LocalDateTime.now());
            stripeWebhookEventRepository.save(webhookEvent);
        } catch (RuntimeException ex) {
            webhookEvent.setStatus(StripeWebhookEventStatus.FAILED);
            webhookEvent.setErrorMessage(truncate(ex.getMessage()));
            stripeWebhookEventRepository.save(webhookEvent);
            throw ex;
        }

        StripeWebhookResponse response = new StripeWebhookResponse();
        response.setEventType(event.getType());
        response.setStatus("processed");
        return response;
    }

    private String resolveSuccessUrl() {
        if (stripeProperties.successUrl() == null || stripeProperties.successUrl().isBlank()) {
            return "https://example.com/subscription/success?session_id={CHECKOUT_SESSION_ID}";
        }
        return stripeProperties.successUrl();
    }

    private String resolveCancelUrl() {
        if (stripeProperties.cancelUrl() == null || stripeProperties.cancelUrl().isBlank()) {
            return "https://example.com/subscription/cancelled";
        }
        return stripeProperties.cancelUrl();
    }

    private void ensureStripeSecretConfigured() {
        if (stripeProperties.secretKey() == null || stripeProperties.secretKey().isBlank()) {
            throw new RuntimeException("Stripe secret key is not configured");
        }
    }

    private long toPence(BigDecimal amountGbp) {
        return amountGbp.multiply(new BigDecimal("100"))
                .setScale(0, RoundingMode.HALF_UP)
                .longValue();
    }

    private String hashPayload(String payload) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(payload.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception ex) {
            throw new RuntimeException("Unable to hash webhook payload", ex);
        }
    }

    private String truncate(String value) {
        if (value == null) {
            return null;
        }
        return value.length() <= 1900 ? value : value.substring(0, 1900);
    }
}
