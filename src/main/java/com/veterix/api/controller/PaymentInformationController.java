package com.veterix.api.controller;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import reactor.core.publisher.Mono;

public class PaymentInformationController {

    public String getPaymentInformation() {
        Stripe.apiKey = "sk_test_51KX3S9Avt7CnDFnFV9A96etKIhbCD33qZKLeQ2TlIU4rNuibMIPteCmSV7kaY8sLJZQdd4LzrMrQ2dlGUKJpAt9p00I5Lngv6e";
        PaymentIntentCreateParams paymentIntentCreateParams = PaymentIntentCreateParams.builder()
                .setAmount(500L)
                .setCurrency("usd")
                .setAutomaticPaymentMethods(
                        PaymentIntentCreateParams.AutomaticPaymentMethods.builder().setEnabled(true).build()
                )
                .build();
        try {
            return PaymentIntent.create(paymentIntentCreateParams).getClientSecret();
        } catch (StripeException e){
            throw new RuntimeException("Unable to create strip payment");
        }

    }
}
