package dev.domaincentric.sample.ecommerce.checkout.domain.model;

import dev.domaincentric.dca.buildingblocks.ddd.tactical.Value;

/**
 * Enum representing the status of a checkout session.
 *
 * <p>Status lifecycle:
 *
 * <ul>
 *   <li>ACTIVE - Session is in progress, can accept step submissions
 *   <li>CONFIRMED - Order has been confirmed, awaiting completion
 *   <li>COMPLETED - Order has been successfully processed
 *   <li>ABANDONED - Session was explicitly abandoned by the user
 *   <li>EXPIRED - Session timed out due to inactivity
 * </ul>
 */
public enum CheckoutSessionStatus implements Value {
  ACTIVE,
  CONFIRMED,
  COMPLETED,
  ABANDONED,
  EXPIRED,
  SUPERSEDED;

  public boolean isModifiable() {
    return this == ACTIVE;
  }

  public boolean isTerminal() {
    return this == SUPERSEDED || this == COMPLETED || this == ABANDONED || this == EXPIRED;
  }

  public boolean canConfirm() {
    return this == ACTIVE;
  }

  public boolean canComplete() {
    return this == CONFIRMED;
  }
}
