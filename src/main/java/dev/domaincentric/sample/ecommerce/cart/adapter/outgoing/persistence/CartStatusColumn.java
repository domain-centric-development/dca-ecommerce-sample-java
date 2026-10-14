package dev.domaincentric.sample.ecommerce.cart.adapter.outgoing.persistence;

import dev.domaincentric.sample.ecommerce.cart.domain.model.CartStatus;

/**
 * How a cart's status is stored: each value's spelling written out, so renaming a member never
 * changes what the store holds. The spellings are the ones every stored cart already carries.
 */
public final class CartStatusColumn {

  private CartStatusColumn() {}

  /**
   * The stored spelling of a status.
   *
   * @param status the status
   * @return the value of the {@code status} column
   */
  public static String valueOf(final CartStatus status) {
    return switch (status) {
      case ACTIVE -> "ACTIVE";
      case COMPLETED -> "COMPLETED";
      case ABANDONED -> "ABANDONED";
    };
  }

  /**
   * The status a stored spelling names.
   *
   * @param value the value of the {@code status} column
   * @return the status
   * @throws IllegalStateException if the store holds a spelling no status has
   */
  public static CartStatus statusOf(final String value) {
    return switch (value) {
      case "ACTIVE" -> CartStatus.ACTIVE;
      case "COMPLETED" -> CartStatus.COMPLETED;
      case "ABANDONED" -> CartStatus.ABANDONED;
      default -> throw new IllegalStateException("Unknown stored cart status: " + value);
    };
  }
}
