package dev.domaincentric.sample.ecommerce.checkout.domain.model;

import org.jspecify.annotations.Nullable;

/** Brings a checkout session into a state the way a repository restores a stored one. */
public final class StoredCheckoutSessions {

  private StoredCheckoutSessions() {}

  /**
   * The same session, read back with another status; step, step data and order reference stay.
   *
   * @param session the session to restore
   * @param status the status it was stored with
   * @return the restored session, with no pending domain events
   */
  public static CheckoutSession withStatus(
      final CheckoutSession session, final CheckoutSessionStatus status) {
    return restored(session, session.currentStep(), status, session.orderReference());
  }

  /**
   * The same session, read back standing at another step; status and step data stay.
   *
   * @param session the session to restore
   * @param step the step it was stored standing at
   * @return the restored session, with no pending domain events
   */
  public static CheckoutSession atStep(final CheckoutSession session, final CheckoutStep step) {
    return restored(session, step, session.status(), session.orderReference());
  }

  /**
   * The same session, read back as completed under an order reference.
   *
   * @param session the session to restore
   * @param orderReference the order reference it was completed under
   * @return the restored session, with no pending domain events
   */
  public static CheckoutSession completed(
      final CheckoutSession session, @Nullable final String orderReference) {
    return restored(
        session, session.currentStep(), CheckoutSessionStatus.COMPLETED, orderReference);
  }

  private static CheckoutSession restored(
      final CheckoutSession session,
      final CheckoutStep step,
      final CheckoutSessionStatus status,
      @Nullable final String orderReference) {
    return CheckoutSession.reconstitute(
        session.id(),
        session.cartId(),
        session.customerId(),
        session.lineItems(),
        session.totals(),
        step,
        status,
        session.buyerInfo(),
        session.deliveryAddress(),
        session.shippingOption(),
        session.paymentSelection(),
        orderReference);
  }
}
