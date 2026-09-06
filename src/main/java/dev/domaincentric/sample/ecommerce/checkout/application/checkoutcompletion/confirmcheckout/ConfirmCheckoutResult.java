package dev.domaincentric.sample.ecommerce.checkout.application.confirmcheckout;

import org.jspecify.annotations.Nullable;

/**
 * Output model for checkout confirmation.
 *
 * @param sessionId the checkout session ID
 * @param currentStep the current step after confirmation (CONFIRMATION)
 * @param status the session status (CONFIRMED)
 * @param cartId the original cart ID
 * @param customerId the customer ID
 * @param totalAmount the total amount in the smallest currency unit
 * @param currency the currency code
 * @param orderReference optional order reference if available
 */
public record ConfirmCheckoutResult(
    String sessionId,
    String currentStep,
    String status,
    String cartId,
    String customerId,
    String totalAmount,
    String currency,
    @Nullable String orderReference) {}
