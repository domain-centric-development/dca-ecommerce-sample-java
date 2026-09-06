package dev.domaincentric.sample.ecommerce.checkout.application.submitpayment;

import org.jspecify.annotations.Nullable;

/**
 * Output model for payment submission.
 *
 * @param sessionId the checkout session ID
 * @param currentStep the current step after submission
 * @param status the session status
 * @param providerId the selected payment provider ID
 * @param providerName the payment provider display name
 * @param providerReference optional provider-specific reference
 */
public record SubmitPaymentResult(
    String sessionId,
    String currentStep,
    String status,
    String providerId,
    String providerName,
    @Nullable String providerReference) {}
