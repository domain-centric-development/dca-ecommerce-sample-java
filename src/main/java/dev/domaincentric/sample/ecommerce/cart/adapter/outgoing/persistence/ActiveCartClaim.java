package dev.domaincentric.sample.ecommerce.cart.adapter.outgoing.persistence;

import java.util.Locale;

/**
 * Recognises the one constraint the relational adapters translate: the claim on a customer's active
 * cart.
 *
 * <p>"At most one active cart per customer" is uniqueness over a subset of the rows, which the
 * database expresses as a unique index over a generated column (ADR-045). Whichever adapter writes
 * the row, the violation arrives as a generic integrity failure, and only the index name says which
 * rule refused. Every other constraint keeps travelling as what it is: a save reported as "this
 * customer already shops in another cart" when a foreign key broke would send the caller looking in
 * the wrong place.
 */
public final class ActiveCartClaim {

  /** The unique index of {@code schema.sql} whose violation this claim is. */
  public static final String INDEX_NAME = "UQ_CARTS_ACTIVE_CUSTOMER";

  private ActiveCartClaim() {}

  /** Whether this failure is the active-cart index refusing a second open cart. */
  public static boolean wasRefused(final Throwable failure) {
    for (Throwable cause = failure; cause != null; cause = cause.getCause()) {
      final String message = cause.getMessage();
      if (message != null && message.toUpperCase(Locale.ROOT).contains(INDEX_NAME)) {
        return true;
      }
      if (cause.getCause() == cause) {
        break;
      }
    }
    return false;
  }
}
