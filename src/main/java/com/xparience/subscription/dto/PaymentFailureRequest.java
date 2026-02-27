package com.xparience.subscription.dto;

public class PaymentFailureRequest {
    private boolean paymentRecovered;

    public boolean isPaymentRecovered() {
        return paymentRecovered;
    }

    public void setPaymentRecovered(boolean paymentRecovered) {
        this.paymentRecovered = paymentRecovered;
    }
}
