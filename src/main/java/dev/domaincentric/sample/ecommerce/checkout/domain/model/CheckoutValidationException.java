package dev.domaincentric.sample.ecommerce.checkout.domain.model;

public final class CheckoutValidationException extends IllegalStateException {
  private final CheckoutValidationResult validation;

  public CheckoutValidationException(CheckoutValidationResult validation) {
    super("Checkout validation failed: " + validation.errors());
    this.validation = validation;
  }

  public CheckoutValidationResult validation() {
    return validation;
  }
}
