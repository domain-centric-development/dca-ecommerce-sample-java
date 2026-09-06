package dev.domaincentric.sample.ecommerce.checkout.application.submitbuyerinfo;

/**
 * Input model for submitting buyer contact information during checkout.
 *
 * @param sessionId the checkout session ID
 * @param email the buyer's email address
 * @param firstName the buyer's first name
 * @param lastName the buyer's last name
 * @param phone the buyer's phone number
 */
public record SubmitBuyerInfoCommand(
    String sessionId, String email, String firstName, String lastName, String phone) {

  /** Compact constructor with validation. */
  public SubmitBuyerInfoCommand {
    if (sessionId == null || sessionId.isBlank()) {
      throw new IllegalArgumentException("Session ID cannot be null or blank");
    }
    if (email == null || email.isBlank()) {
      throw new IllegalArgumentException("Email cannot be null or blank");
    }
    if (firstName == null || firstName.isBlank()) {
      throw new IllegalArgumentException("First name cannot be null or blank");
    }
    if (lastName == null || lastName.isBlank()) {
      throw new IllegalArgumentException("Last name cannot be null or blank");
    }
    if (phone == null || phone.isBlank()) {
      throw new IllegalArgumentException("Phone cannot be null or blank");
    }
  }
}
