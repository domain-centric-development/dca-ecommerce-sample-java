package dev.domaincentric.sample.ecommerce.checkout.domain.readmodel;

import dev.domaincentric.dca.buildingblocks.ddd.tactical.Value;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.BuyerInfo;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CartId;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutSession;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutSessionId;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutSessionStatus;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutStep;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutTotals;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CustomerId;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.DeliveryAddress;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.PaymentSelection;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.ShippingOption;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.StepAccess;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Money;
import java.util.List;
import org.jspecify.annotations.Nullable;

/**
 * Read Model representing a snapshot of checkout session state.
 *
 * <p>This immutable read model contains all state from the {@link CheckoutSession} aggregate,
 * providing a query-optimized view for display purposes.
 *
 * <p>Use the {@link #from(CheckoutSession)} factory method to create a snapshot from a checkout
 * session aggregate.
 */
public record CheckoutCartSnapshot(
    CheckoutSessionId sessionId,
    CartId cartId,
    CustomerId customerId,
    CheckoutStep step,
    CheckoutSessionStatus status,
    List<LineItemSnapshot> lineItems,
    Money subtotal,
    @Nullable CheckoutTotals totals,
    @Nullable BuyerInfo buyerInfo,
    @Nullable DeliveryAddress deliveryAddress,
    @Nullable ShippingOption shippingOption,
    @Nullable PaymentSelection paymentSelection,
    @Nullable String orderReference)
    implements Value {

  public CheckoutCartSnapshot {
    if (sessionId == null) {
      throw new IllegalArgumentException("Session ID cannot be null");
    }
    if (cartId == null) {
      throw new IllegalArgumentException("Cart ID cannot be null");
    }
    if (customerId == null) {
      throw new IllegalArgumentException("Customer ID cannot be null");
    }
    if (step == null) {
      throw new IllegalArgumentException("Step cannot be null");
    }
    if (status == null) {
      throw new IllegalArgumentException("Status cannot be null");
    }
    if (lineItems == null) {
      throw new IllegalArgumentException("Line items cannot be null");
    }
    if (subtotal == null) {
      throw new IllegalArgumentException("Subtotal cannot be null");
    }
    // Make defensive copy
    lineItems = List.copyOf(lineItems);
  }

  /**
   * Creates a CheckoutCartSnapshot from a CheckoutSession aggregate.
   *
   * <p>This factory method extracts all relevant state from the aggregate to create an immutable
   * read model for display purposes.
   *
   * @param session the checkout session aggregate
   * @return a snapshot of the session state
   */
  public static CheckoutCartSnapshot from(final CheckoutSession session) {
    if (session == null) {
      throw new IllegalArgumentException("Session cannot be null");
    }

    final List<LineItemSnapshot> lineItemSnapshots =
        session.lineItems().stream()
            .map(
                item ->
                    new LineItemSnapshot(
                        item.id(),
                        item.productId(),
                        item.productName(),
                        item.unitPrice(),
                        item.quantity(),
                        item.imageUrl()))
            .toList();

    return new CheckoutCartSnapshot(
        session.id(),
        session.cartId(),
        session.customerId(),
        session.currentStep(),
        session.status(),
        lineItemSnapshots,
        session.totals().subtotal(),
        session.totals(),
        session.buyerInfo(),
        session.deliveryAddress(),
        session.shippingOption(),
        session.paymentSelection(),
        session.orderReference());
  }

  /**
   * Returns the number of distinct line items.
   *
   * @return the count of line items
   */
  public int itemCount() {
    return lineItems.size();
  }

  /**
   * Returns the total quantity across all items.
   *
   * @return the sum of all item quantities
   */
  public int totalQuantity() {
    return lineItems.stream().mapToInt(LineItemSnapshot::quantity).sum();
  }

  /**
   * Checks if buyer info has been submitted.
   *
   * @return true if buyer info is present
   */
  public boolean hasBuyerInfo() {
    return buyerInfo != null;
  }

  /**
   * Checks if delivery address has been submitted.
   *
   * @return true if delivery address is present
   */
  public boolean hasDeliveryAddress() {
    return deliveryAddress != null;
  }

  /**
   * Checks if shipping option has been selected.
   *
   * @return true if shipping option is present
   */
  public boolean hasShippingOption() {
    return shippingOption != null;
  }

  /**
   * Checks if payment has been selected.
   *
   * @return true if payment selection is present
   */
  public boolean hasPaymentSelection() {
    return paymentSelection != null;
  }

  /**
   * Checks if the checkout has totals calculated.
   *
   * @return true if totals are present
   */
  public boolean hasTotals() {
    return totals != null;
  }

  /**
   * Checks if the checkout has an order reference.
   *
   * @return true if order reference is present
   */
  public boolean hasOrderReference() {
    return orderReference != null;
  }

  /**
   * Checks whether the data a step contributes is present on the session.
   *
   * @param step the step to check
   * @return true if the step has been fulfilled
   */
  public boolean isStepCompleted(final CheckoutStep step) {
    return switch (step) {
      case BUYER_INFO -> hasBuyerInfo();
      case DELIVERY -> hasDeliveryAddress() && hasShippingOption();
      case PAYMENT -> hasPaymentSelection();
      case REVIEW -> isConfirmed() || isCompleted();
      case CONFIRMATION -> isCompleted();
    };
  }

  /**
   * Checks if the session is active.
   *
   * @return true if status is ACTIVE
   */
  public boolean isActive() {
    return status == CheckoutSessionStatus.ACTIVE;
  }

  /**
   * Checks if the session is completed.
   *
   * @return true if status is COMPLETED
   */
  public boolean isCompleted() {
    return status == CheckoutSessionStatus.COMPLETED;
  }

  /**
   * Checks if the session is confirmed.
   *
   * @return true if status is CONFIRMED
   */
  public boolean isConfirmed() {
    return status == CheckoutSessionStatus.CONFIRMED;
  }

  /**
   * Whether this snapshot may be shown at the requested step, and where to send the customer
   * instead.
   *
   * <p>The rule is the checkout's own business logic, not a presentation concern: it holds whether
   * the user interface shows one page or five. It decides on the snapshot alone, so a read does not
   * have to load the session aggregate.
   */
  public StepAccess accessTo(final CheckoutStep targetStep) {

    // Rule 2: Terminal states handling (COMPLETED, ABANDONED, EXPIRED)
    if (status().isTerminal()) {
      return handleTerminalState(targetStep);
    }

    // Rule 3: CONFIRMED state - can only access CONFIRMATION
    if (status().canComplete()) {
      return handleConfirmedState(targetStep);
    }

    // Rule 4: CONFIRMATION step is only accessible after checkout is confirmed or completed
    if (targetStep == CheckoutStep.CONFIRMATION) {
      return handleConfirmationAccess();
    }

    // Rule 5: Cannot skip ahead - must complete prior steps
    if (isSkippingAhead(targetStep)) {
      return StepAccess.redirectTo(step());
    }

    return StepAccess.grant();
  }

  private StepAccess handleTerminalState(final CheckoutStep targetStep) {

    return switch (status()) {
      case COMPLETED -> {
        // Completed sessions can only access CONFIRMATION
        if (targetStep == CheckoutStep.CONFIRMATION) {
          yield StepAccess.grant();
        }
        yield StepAccess.redirectTo(CheckoutStep.CONFIRMATION);
      }
      case ABANDONED, EXPIRED -> {
        // Abandoned/expired sessions send the customer back to the cart to start fresh
        yield StepAccess.backToCart();
      }
      // CONFIRMED is not terminal - handled separately by handleConfirmedState
      // ACTIVE is not terminal - handled by normal flow
      default -> StepAccess.grant();
    };
  }

  private StepAccess handleConfirmedState(final CheckoutStep targetStep) {
    // CONFIRMED sessions can only access CONFIRMATION step
    if (targetStep == CheckoutStep.CONFIRMATION) {
      return StepAccess.grant();
    }
    return StepAccess.redirectTo(CheckoutStep.CONFIRMATION);
  }

  private StepAccess handleConfirmationAccess() {
    // CONFIRMATION is only accessible when status is CONFIRMED or COMPLETED
    if (isConfirmed() || isCompleted()) {
      return StepAccess.grant();
    }

    // Back to the current step if trying to access CONFIRMATION prematurely
    return StepAccess.redirectTo(step());
  }

  private boolean isSkippingAhead(final CheckoutStep targetStep) {
    // Check if user is trying to access a step beyond their current progress
    final CheckoutStep currentStep = step();

    // Cannot go to a step that comes after the current step
    if (targetStep.isAfter(currentStep)) {
      return true;
    }

    // For steps before or equal to current, also verify prerequisites are met
    // (going back is allowed, but going to a step whose prerequisites aren't met is not)
    return !arePrerequisitesMet(targetStep);
  }

  private boolean arePrerequisitesMet(final CheckoutStep targetStep) {
    return switch (targetStep) {
      case BUYER_INFO -> true; // First step, no prerequisites
      case DELIVERY -> isStepCompleted(CheckoutStep.BUYER_INFO);
      case PAYMENT ->
          isStepCompleted(CheckoutStep.BUYER_INFO) && isStepCompleted(CheckoutStep.DELIVERY);
      case REVIEW ->
          isStepCompleted(CheckoutStep.BUYER_INFO)
              && isStepCompleted(CheckoutStep.DELIVERY)
              && isStepCompleted(CheckoutStep.PAYMENT);
      case CONFIRMATION -> isCompleted();
    };
  }
}
