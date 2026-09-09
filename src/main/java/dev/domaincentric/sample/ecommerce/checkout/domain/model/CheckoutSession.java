package dev.domaincentric.sample.ecommerce.checkout.domain.model;

import dev.domaincentric.dca.buildingblocks.ddd.tactical.BaseAggregateRoot;
import dev.domaincentric.sample.ecommerce.checkout.domain.event.BuyerInfoSubmitted;
import dev.domaincentric.sample.ecommerce.checkout.domain.event.CheckoutAbandoned;
import dev.domaincentric.sample.ecommerce.checkout.domain.event.CheckoutCompleted;
import dev.domaincentric.sample.ecommerce.checkout.domain.event.CheckoutConfirmed;
import dev.domaincentric.sample.ecommerce.checkout.domain.event.CheckoutExpired;
import dev.domaincentric.sample.ecommerce.checkout.domain.event.CheckoutSessionStarted;
import dev.domaincentric.sample.ecommerce.checkout.domain.event.DeliverySubmitted;
import dev.domaincentric.sample.ecommerce.checkout.domain.event.PaymentSubmitted;
import dev.domaincentric.sample.ecommerce.checkout.domain.service.TaxCalculator;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Money;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.ProductId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.jspecify.annotations.Nullable;

/**
 * CheckoutSession Aggregate Root.
 *
 * <p>Represents a customer's checkout session with state management across a 5-step checkout flow:
 * Buyer Info → Delivery → Payment → Review → Confirmation.
 *
 * <p><b>Business Rules:</b>
 *
 * <ul>
 *   <li>Cannot skip steps - must complete each step in order
 *   <li>Can go back to previous steps to modify data
 *   <li>Cannot modify a confirmed, completed, abandoned, or expired session
 *   <li>Must have at least one line item to start checkout
 *   <li>Must complete all steps before confirmation
 * </ul>
 *
 * <p><b>Domain Events:</b>
 *
 * <ul>
 *   <li>{@link CheckoutSessionStarted} - when checkout begins from cart
 *   <li>{@link BuyerInfoSubmitted} - when buyer info step is completed
 *   <li>{@link DeliverySubmitted} - when delivery step is completed
 *   <li>{@link PaymentSubmitted} - when payment step is completed
 *   <li>{@link CheckoutConfirmed} - when order is confirmed at review
 *   <li>{@link CheckoutCompleted} - when checkout is fully processed
 *   <li>{@link CheckoutAbandoned} - when customer abandons checkout
 *   <li>{@link CheckoutExpired} - when session expires due to inactivity
 * </ul>
 */
public final class CheckoutSession extends BaseAggregateRoot<CheckoutSession, CheckoutSessionId> {

  private final CheckoutSessionId id;
  private final CartId cartId;
  private final CustomerId customerId;
  private final List<CheckoutLineItem> lineItems;
  private CheckoutTotals totals;
  private CheckoutStep currentStep;
  private CheckoutSessionStatus status;

  // Step data (nullable until step is completed)
  private BuyerInfo buyerInfo;
  private DeliveryAddress deliveryAddress;
  private ShippingOption shippingOption;
  private PaymentSelection paymentSelection;

  // Order reference after completion
  private String orderReference;

  private CheckoutSession(
      final CheckoutSessionId id,
      final CartId cartId,
      final CustomerId customerId,
      final List<CheckoutLineItem> lineItems,
      final Money subtotal,
      final TaxCalculator taxCalculator) {
    this.id = id;
    this.cartId = cartId;
    this.customerId = customerId;
    this.lineItems = new ArrayList<>(lineItems);
    this.totals =
        CheckoutTotals.calculate(
            subtotal, Money.zero(subtotal.currency()), taxCalculator.containedTax(subtotal));
    this.currentStep = CheckoutStep.BUYER_INFO;
    this.status = CheckoutSessionStatus.ACTIVE;
  }

  /**
   * Factory method to start a new checkout session.
   *
   * <p>Raises a {@link CheckoutSessionStarted} domain event.
   *
   * @param cartId the cart ID this checkout originates from
   * @param customerId the customer ID (may be guest)
   * @param lineItems the line items from the cart
   * @param subtotal the subtotal of all line items
   * @param taxCalculator resolves the tax contained in the totals
   * @return a new checkout session
   * @throws IllegalArgumentException if lineItems is empty
   */
  public static CheckoutSession start(
      final CartId cartId,
      final CustomerId customerId,
      final List<CheckoutLineItem> lineItems,
      final Money subtotal,
      final TaxCalculator taxCalculator) {
    if (lineItems == null || lineItems.isEmpty()) {
      throw new IllegalArgumentException("Cannot start checkout with empty line items");
    }

    final CheckoutSessionId sessionId = CheckoutSessionId.generate();
    final CheckoutSession session =
        new CheckoutSession(sessionId, cartId, customerId, lineItems, subtotal, taxCalculator);

    session.registerEvent(
        CheckoutSessionStarted.now(sessionId, cartId, customerId, subtotal, lineItems.size()));

    return session;
  }

  /** Replaced by a new explicit checkout action; confirmed orders cannot be superseded. */
  public void supersede() {
    ensureModifiable();
    this.status = CheckoutSessionStatus.SUPERSEDED;
  }

  @Override
  public CheckoutSessionId id() {
    return id;
  }

  public CartId cartId() {
    return cartId;
  }

  public CustomerId customerId() {
    return customerId;
  }

  public List<CheckoutLineItem> lineItems() {
    return Collections.unmodifiableList(lineItems);
  }

  public CheckoutTotals totals() {
    return totals;
  }

  public CheckoutStep currentStep() {
    return currentStep;
  }

  public CheckoutSessionStatus status() {
    return status;
  }

  @Nullable
  public BuyerInfo buyerInfo() {
    return buyerInfo;
  }

  @Nullable
  public DeliveryAddress deliveryAddress() {
    return deliveryAddress;
  }

  @Nullable
  public ShippingOption shippingOption() {
    return shippingOption;
  }

  @Nullable
  public PaymentSelection paymentSelection() {
    return paymentSelection;
  }

  @Nullable
  public String orderReference() {
    return orderReference;
  }

  /**
   * Synchronizes line items with the current cart state.
   *
   * <p>This method updates the checkout session's line items when the underlying cart changes
   * during the checkout flow. It recalculates the subtotal and updates totals accordingly.
   *
   * @param newLineItems the updated line items from the cart
   * @param newSubtotal the new subtotal calculated from the cart
   * @param taxCalculator resolves the tax contained in the totals
   * @throws IllegalStateException if session is not modifiable
   * @throws IllegalArgumentException if newLineItems is empty
   */
  public void syncLineItems(
      final List<CheckoutLineItem> newLineItems,
      final Money newSubtotal,
      final TaxCalculator taxCalculator) {
    throw new IllegalStateException("Checkout snapshots are immutable; start a new session");
  }

  /**
   * Submits buyer information for the checkout.
   *
   * <p>Raises a {@link BuyerInfoSubmitted} domain event.
   *
   * @param buyerInfo the buyer contact information
   * @throws IllegalStateException if session is not modifiable or if trying to skip steps
   */
  public void submitBuyerInfo(final BuyerInfo buyerInfo) {
    ensureModifiable();
    ensureAtOrBeforeStep(CheckoutStep.BUYER_INFO);

    this.buyerInfo = buyerInfo;

    // Advance to next step if currently at buyer info step
    if (currentStep == CheckoutStep.BUYER_INFO) {
      this.currentStep = CheckoutStep.DELIVERY;
    }

    registerEvent(BuyerInfoSubmitted.now(this.id, buyerInfo));
  }

  /**
   * Submits delivery information for the checkout.
   *
   * <p>Raises a {@link DeliverySubmitted} domain event.
   *
   * @param address the delivery address
   * @param shippingOption the selected shipping option
   * @param taxCalculator resolves the tax contained in the totals
   * @throws IllegalStateException if session is not modifiable or if trying to skip steps
   */
  public void submitDelivery(
      final DeliveryAddress address,
      final ShippingOption shippingOption,
      final TaxCalculator taxCalculator) {
    ensureModifiable();
    ensureStepCompleted(CheckoutStep.BUYER_INFO);
    ensureAtOrBeforeStep(CheckoutStep.DELIVERY);

    this.deliveryAddress = address;
    this.shippingOption = shippingOption;

    // Update totals with shipping cost; the tax contained in them moves with it
    final CheckoutTotals withShipping = this.totals.withShipping(shippingOption.cost());
    this.totals =
        withShipping.withTax(
            taxCalculator.containedTax(withShipping.subtotal().add(withShipping.shipping())));

    // Advance to next step if currently at delivery step
    if (currentStep == CheckoutStep.DELIVERY) {
      this.currentStep = CheckoutStep.PAYMENT;
    }

    registerEvent(DeliverySubmitted.now(this.id, address, shippingOption));
  }

  /**
   * Submits payment method selection for the checkout.
   *
   * <p>Raises a {@link PaymentSubmitted} domain event.
   *
   * @param payment the payment method selection
   * @throws IllegalStateException if session is not modifiable or if trying to skip steps
   */
  public void submitPayment(final PaymentSelection payment) {
    ensureModifiable();
    ensureStepCompleted(CheckoutStep.BUYER_INFO);
    ensureStepCompleted(CheckoutStep.DELIVERY);
    ensureAtOrBeforeStep(CheckoutStep.PAYMENT);

    this.paymentSelection = payment;

    // Advance to next step if currently at payment step
    if (currentStep == CheckoutStep.PAYMENT) {
      this.currentStep = CheckoutStep.REVIEW;
    }

    registerEvent(PaymentSubmitted.now(this.id, payment));
  }

  /**
   * Calculates the order total using fresh pricing data from the resolver.
   *
   * <p>Iterates through all line items and resolves current prices to compute the sum of (current
   * price × quantity) for each item.
   *
   * @param facts the facts providing current pricing information
   * @return the calculated order total
   */
  public Money calculateOrderTotal(
      java.util.Map<ProductId, CheckoutArticlePriceResolver.ArticlePrice> facts) {
    return new dev.domaincentric.sample.ecommerce.checkout.domain.service.CheckoutPricing()
        .calculateOrderTotal(lineItems, facts, totals.subtotal().currency());
  }

  /**
   * Validates checkout items against current pricing and availability data.
   *
   * <p>Checks each line item for:
   *
   * <ul>
   *   <li>Product availability
   *   <li>Sufficient stock for the requested quantity
   * </ul>
   *
   * @param facts the facts providing current pricing and availability information
   * @return a validation result containing any errors found
   */
  public CheckoutValidationResult validateItems(
      java.util.Map<ProductId, CheckoutArticlePriceResolver.ArticlePrice> facts) {
    return new dev.domaincentric.sample.ecommerce.checkout.domain.service.CheckoutPricing()
        .validateItems(lineItems, facts, totals.subtotal().currency());
  }

  /**
   * Confirms the checkout order after validating items with fresh pricing data.
   *
   * <p>Validates all items using the resolver before confirming. If validation fails, an exception
   * is thrown with details about the validation errors.
   *
   * <p>Raises a {@link CheckoutConfirmed} domain event on success.
   *
   * @param facts the resolver for validating current pricing and availability
   * @throws IllegalStateException if session is not confirmable, steps are incomplete, or
   *     validation fails
   */
  public void confirm(java.util.Map<ProductId, CheckoutArticlePriceResolver.ArticlePrice> facts) {
    ensureModifiable();
    ensureAllStepsCompleted();

    if (currentStep != CheckoutStep.REVIEW) {
      throw new IllegalStateException("Can only confirm from review step");
    }

    final CheckoutValidationResult validationResult = validateItems(facts);
    if (!validationResult.isValid()) {
      throw new CheckoutValidationException(validationResult);
    }

    final var recomputed = calculateOrderTotal(facts);
    this.totals =
        CheckoutTotals.calculate(
            recomputed,
            totals.shipping(),
            new TaxCalculator().containedTax(recomputed.add(totals.shipping())));
    this.status = CheckoutSessionStatus.CONFIRMED;
    this.currentStep = CheckoutStep.CONFIRMATION;

    registerEvent(
        CheckoutConfirmed.now(
            this.id, this.cartId, this.customerId, this.totals.total(), this.lineItems));
  }

  /**
   * Completes the checkout after successful payment processing.
   *
   * <p>Raises a {@link CheckoutCompleted} domain event.
   *
   * @param orderReference optional order reference from order system
   * @throws IllegalStateException if session is not in confirmed status
   */
  public void complete(@Nullable final String orderReference) {
    if (!status.canComplete()) {
      throw new IllegalStateException("Cannot complete checkout with status: " + status);
    }

    this.orderReference = orderReference;
    this.status = CheckoutSessionStatus.COMPLETED;

    registerEvent(CheckoutCompleted.now(this.id, this.totals.total(), orderReference));
  }

  /**
   * Abandons the checkout session.
   *
   * <p>Raises a {@link CheckoutAbandoned} domain event.
   *
   * @throws IllegalStateException if session is already in a terminal state
   */
  public void abandon() {
    if (!status.isModifiable()) {
      throw new IllegalStateException("Cannot abandon checkout with status: " + status);
    }

    final CheckoutStep abandonedAt = this.currentStep;
    this.status = CheckoutSessionStatus.ABANDONED;

    registerEvent(CheckoutAbandoned.now(this.id, abandonedAt));
  }

  /**
   * Expires the checkout session due to inactivity.
   *
   * <p>Raises a {@link CheckoutExpired} domain event.
   *
   * @throws IllegalStateException if session is already in a terminal state
   */
  public void expire() {
    if (!status.isModifiable()) {
      throw new IllegalStateException("Cannot expire checkout with status: " + status);
    }

    final CheckoutStep expiredAt = this.currentStep;
    this.status = CheckoutSessionStatus.EXPIRED;

    registerEvent(CheckoutExpired.now(this.id, expiredAt));
  }

  /**
   * Navigates back to a previous step.
   *
   * @param step the step to navigate back to
   * @throws IllegalStateException if session is not modifiable
   * @throws IllegalArgumentException if trying to go forward or to confirmation step
   */
  public void goBackTo(final CheckoutStep step) {
    ensureModifiable();

    if (step == CheckoutStep.CONFIRMATION) {
      throw new IllegalArgumentException("Cannot navigate directly to confirmation step");
    }
    if (step.isAfter(currentStep)) {
      throw new IllegalArgumentException(
          "Cannot skip forward to step " + step + " from " + currentStep);
    }

    this.currentStep = step;
  }

  /**
   * Checks if a specific step has been completed with data.
   *
   * @param step the step to check
   * @return true if the step data has been submitted
   */
  public boolean isStepCompleted(final CheckoutStep step) {
    return switch (step) {
      case BUYER_INFO -> buyerInfo != null;
      case DELIVERY -> deliveryAddress != null && shippingOption != null;
      case PAYMENT -> paymentSelection != null;
      case REVIEW ->
          status == CheckoutSessionStatus.CONFIRMED || status == CheckoutSessionStatus.COMPLETED;
      case CONFIRMATION -> status == CheckoutSessionStatus.COMPLETED;
    };
  }

  /**
   * Checks if the session is active and can be modified.
   *
   * @return true if status is ACTIVE
   */
  public boolean isActive() {
    return status == CheckoutSessionStatus.ACTIVE;
  }

  /**
   * Checks if the session has been completed successfully.
   *
   * @return true if status is COMPLETED
   */
  public boolean isCompleted() {
    return status == CheckoutSessionStatus.COMPLETED;
  }

  private void ensureModifiable() {
    if (!status.isModifiable()) {
      throw new IllegalStateException("Cannot modify checkout with status: " + status);
    }
  }

  private void ensureStepCompleted(final CheckoutStep step) {
    if (!isStepCompleted(step)) {
      throw new IllegalStateException("Step " + step + " must be completed first");
    }
  }

  private void ensureAtOrBeforeStep(final CheckoutStep step) {
    // Allow modifying current step or going back to modify previous steps
    // Cannot skip forward (e.g., submit payment before delivery)
    if (currentStep.isBefore(step)) {
      throw new IllegalStateException(
          "Cannot skip to step " + step + " - currently at " + currentStep);
    }
  }

  private void ensureAllStepsCompleted() {
    if (!isStepCompleted(CheckoutStep.BUYER_INFO)) {
      throw new IllegalStateException("Buyer info not submitted");
    }
    if (!isStepCompleted(CheckoutStep.DELIVERY)) {
      throw new IllegalStateException("Delivery not submitted");
    }
    if (!isStepCompleted(CheckoutStep.PAYMENT)) {
      throw new IllegalStateException("Payment not submitted");
    }
  }
}
