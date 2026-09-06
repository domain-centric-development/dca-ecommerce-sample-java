package dev.domaincentric.sample.ecommerce.checkout.application.submitbuyerinfo;

/**
 * Output model for buyer info submission.
 *
 * @param sessionId the checkout session ID
 * @param currentStep the current step after submission
 * @param status the session status
 * @param email the submitted email address
 * @param firstName the submitted first name
 * @param lastName the submitted last name
 * @param phone the submitted phone number
 */
public record SubmitBuyerInfoResult(
    String sessionId,
    String currentStep,
    String status,
    String email,
    String firstName,
    String lastName,
    String phone) {}
