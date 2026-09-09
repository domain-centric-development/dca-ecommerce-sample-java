package dev.domaincentric.sample.ecommerce.checkout.domain.model;

import dev.domaincentric.dca.buildingblocks.ddd.tactical.Value;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.ProductId;
import java.util.List;

/**
 * Value Object representing the result of validating checkout items against current pricing and
 * availability data.
 *
 * <p>Collects validation errors encountered during checkout validation, such as unavailable
 * products or insufficient stock.
 *
 * @param errors the list of validation errors, empty if validation passed
 */
public record CheckoutValidationResult(List<ValidationError> errors) implements Value {

  public CheckoutValidationResult {
    errors = List.copyOf(errors);
  }

  /**
   * Returns whether the validation passed with no errors.
   *
   * @return true if there are no validation errors
   */
  public boolean isValid() {
    return errors.isEmpty();
  }

  /**
   * Creates a valid result with no errors.
   *
   * @return a CheckoutValidationResult with an empty error list
   */
  public static CheckoutValidationResult valid() {
    return new CheckoutValidationResult(List.of());
  }

  /**
   * Creates a result with the specified errors.
   *
   * @param errors the validation errors
   * @return a CheckoutValidationResult containing the errors
   */
  public static CheckoutValidationResult withErrors(List<ValidationError> errors) {
    return new CheckoutValidationResult(errors);
  }

  /**
   * Creates a result with a single error.
   *
   * @param error the validation error
   * @return a CheckoutValidationResult containing the single error
   */
  public static CheckoutValidationResult withError(ValidationError error) {
    return new CheckoutValidationResult(List.of(error));
  }

  /**
   * Value Object representing a single validation error for a checkout item.
   *
   * @param productId the product that failed validation
   * @param message a human-readable error message
   * @param type the type of validation error
   */
  public record ValidationError(ProductId productId, String message, ErrorType type)
      implements Value {

    public ValidationError {
      if (message == null || message.isBlank()) {
        throw new IllegalArgumentException("Error message cannot be null or blank");
      }
    }

    /**
     * Creates an error for an unavailable product.
     *
     * @param productId the unavailable product
     * @return a ValidationError with PRODUCT_UNAVAILABLE type
     */
    public static ValidationError productUnavailable(ProductId productId) {
      return new ValidationError(
          productId, "Product is not available for purchase", ErrorType.PRODUCT_UNAVAILABLE);
    }

    /**
     * Creates an error for insufficient stock.
     *
     * @param productId the product with insufficient stock
     * @param requested the requested quantity
     * @param available the available quantity
     * @return a ValidationError with INSUFFICIENT_STOCK type
     */
    public static ValidationError insufficientStock(
        ProductId productId, int requested, int available) {
      return new ValidationError(
          productId,
          String.format("Insufficient stock: requested %d, available %d", requested, available),
          ErrorType.INSUFFICIENT_STOCK);
    }
  }

  /** Types of validation errors that can occur during checkout validation. */
  public enum ErrorType {
    /** The product is not available for purchase. */
    PRODUCT_UNAVAILABLE,
    /** The requested quantity exceeds available stock. */
    INSUFFICIENT_STOCK,
    PRICE_CHANGED
  }
}
