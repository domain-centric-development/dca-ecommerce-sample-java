package dev.domaincentric.sample.ecommerce.checkout.domain.model;

import dev.domaincentric.dca.buildingblocks.ddd.tactical.BaseAggregateRoot;
import dev.domaincentric.dca.buildingblocks.ddd.tactical.Owned;
import dev.domaincentric.sample.ecommerce.checkout.domain.event.BuyerInfoSubmitted;
import dev.domaincentric.sample.ecommerce.checkout.domain.event.CheckoutConfirmed;
import dev.domaincentric.sample.ecommerce.checkout.domain.event.CheckoutSessionStarted;
import dev.domaincentric.sample.ecommerce.checkout.domain.event.DeliverySubmitted;
import dev.domaincentric.sample.ecommerce.checkout.domain.event.PaymentSubmitted;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Money;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.ProductId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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
 *   <li>{@link PaymentSubmitted} - when the provider authorized the payment at the review
 *   <li>{@link CheckoutConfirmed} - when order is confirmed at review
 * </ul>
 */
public final class CheckoutSession extends BaseAggregateRoot<CheckoutSession, CheckoutSessionId>
    implements Owned<CustomerId> {

  private final CheckoutSessionId id;
  private final CartId cartId;
  private CustomerId customerId;
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
      final Money subtotal) {
    this.id = id;
    this.cartId = cartId;
    this.customerId = customerId;
    this.lineItems = new ArrayList<>(lineItems);
    this.totals = CheckoutTotals.calculate(subtotal, Money.zero(subtotal.currency()));
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
   * @return a new checkout session
   * @throws EmptyCheckoutException if lineItems is empty
   */
  public static CheckoutSession start(
      final CartId cartId,
      final CustomerId customerId,
      final List<CheckoutLineItem> lineItems,
      final Money subtotal) {
    if (lineItems == null || lineItems.isEmpty()) {
      throw new EmptyCheckoutException(cartId);
    }

    final CheckoutSessionId sessionId = CheckoutSessionId.generate();
    final CheckoutSession session =
        new CheckoutSession(sessionId, cartId, customerId, lineItems, subtotal);

    session.registerEvent(
        CheckoutSessionStarted.now(sessionId, cartId, customerId, subtotal, lineItems.size()));

    return session;
  }

  /**
   * Restores a stored checkout session as it was saved, without raising a domain event: nothing
   * happened, it was only read back. The step data is null where the step was not completed yet.
   *
   * @param id the session's id
   * @param cartId the cart it originates from
   * @param customerId the customer it belongs to
   * @param lineItems the line items
   * @param totals the totals
   * @param currentStep the step the session stands at
   * @param status the session's status
   * @param buyerInfo the buyer, or null
   * @param deliveryAddress the delivery address, or null
   * @param shippingOption the shipping option, or null
   * @param paymentSelection the payment, or null
   * @param orderReference the order reference after completion, or null
   * @return the reconstituted session, with no pending domain events
   */
  public static CheckoutSession reconstitute(
      final CheckoutSessionId id,
      final CartId cartId,
      final CustomerId customerId,
      final List<CheckoutLineItem> lineItems,
      final CheckoutTotals totals,
      final CheckoutStep currentStep,
      final CheckoutSessionStatus status,
      @Nullable final BuyerInfo buyerInfo,
      @Nullable final DeliveryAddress deliveryAddress,
      @Nullable final ShippingOption shippingOption,
      @Nullable final PaymentSelection paymentSelection,
      @Nullable final String orderReference) {
    final CheckoutSession session =
        new CheckoutSession(id, cartId, customerId, lineItems, totals.subtotal());
    session.totals = totals;
    session.currentStep = currentStep;
    session.status = status;
    session.buyerInfo = buyerInfo;
    session.deliveryAddress = deliveryAddress;
    session.shippingOption = shippingOption;
    session.paymentSelection = paymentSelection;
    session.orderReference = orderReference;
    return session;
  }

  /**
   * The customer takes the session over: the same session, at the same step, with the same line
   * items, is theirs now. The caller states that this customer owns the session's cart; the session
   * does not check it.
   *
   * @param customer the customer the session belongs to from now on
   * @throws CheckoutNotModifiableException if the session no longer takes changes
   */
  public void takeOverBy(final CustomerId customer) {
    ensureModifiable();
    this.customerId = customer;
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
   * Refuses to update the line items of a running session.
   *
   * <p>A session is the snapshot the customer decided on. When the cart changes underneath it, the
   * answer is a new session over the new contents, not a session whose totals move while somebody
   * is looking at them. The method exists to say so at the call site rather than in prose.
   *
   * @param newLineItems the updated line items from the cart
   * @param newSubtotal the new subtotal calculated from the cart
   * @throws UnsupportedOperationException always — this is not an operation the model has, which is
   *     a statement about the model and not a rule a caller can satisfy
   */
  public void syncLineItems(final List<CheckoutLineItem> newLineItems, final Money newSubtotal) {
    throw new UnsupportedOperationException(
        "Checkout snapshots are immutable; start a new session");
  }

  /**
   * The review of this session at today's prices: what the review page shows and what "Place Order"
   * orders. Changes nothing on the session.
   *
   * @param todaysPrices today's unit price per product; a line without one keeps its own
   * @return the review
   */
  public CheckoutReview reviewAt(final Map<ProductId, Money> todaysPrices) {
    final List<CheckoutLineItem> lines =
        lineItems.stream()
            .map(
                item ->
                    item.withUnitPrice(
                        todaysPrices.getOrDefault(item.productId(), item.unitPrice())))
            .toList();
    final Money subtotal =
        lines.stream()
            .map(CheckoutLineItem::lineTotal)
            .reduce(Money.zero(totals.subtotal().currency()), Money::add);
    return new CheckoutReview(
        id,
        lines,
        CheckoutTotals.calculate(subtotal, totals.shipping()),
        buyerInfo,
        deliveryAddress,
        shippingOption,
        paymentSelection);
  }

  /**
   * Asserts that the review at today's prices is the one the customer was shown.
   *
   * @param todaysPrices today's unit price per product
   * @param shown the fingerprint of the review the customer was shown
   * @return the review, as the customer was shown it
   * @throws OrderChangedSinceReviewException if the review has changed since
   */
  public CheckoutReview assertReviewed(
      final Map<ProductId, Money> todaysPrices, final ReviewFingerprint shown) {
    final CheckoutReview review = reviewAt(todaysPrices);
    review.refuseUnlessShown(shown);
    return review;
  }

  /**
   * Submits buyer information for the checkout.
   *
   * <p>Raises a {@link BuyerInfoSubmitted} domain event.
   *
   * @param buyerInfo the buyer contact information
   * @throws CheckoutNotModifiableException if the session no longer takes changes
   * @throws CheckoutStepOutOfOrderException if the step is ahead of the current one
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
   * @throws CheckoutNotModifiableException if the session no longer takes changes
   * @throws CheckoutStepOutOfOrderException if the step is ahead of the current one
   */
  public void submitDelivery(final DeliveryAddress address, final ShippingOption shippingOption) {
    ensureModifiable();
    ensureStepCompleted(CheckoutStep.BUYER_INFO);
    ensureAtOrBeforeStep(CheckoutStep.DELIVERY);

    this.deliveryAddress = address;
    this.shippingOption = shippingOption;

    // Update totals with shipping cost; the tax contained in them moves with it
    this.totals = this.totals.withShipping(shippingOption.cost());

    // Advance to next step if currently at delivery step
    if (currentStep == CheckoutStep.DELIVERY) {
      this.currentStep = CheckoutStep.PAYMENT;
    }

    registerEvent(DeliverySubmitted.now(this.id, address, shippingOption));
  }

  /**
   * Submits payment method selection for the checkout: it records how the customer pays, nothing is
   * requested from the provider yet. A way to pay is chosen only for a session that has something
   * to pay.
   *
   * @param payment the payment method selection
   * @throws CheckoutNotModifiableException if the session no longer takes changes
   * @throws CheckoutStepNotCompletedException if the buyer or delivery step is missing
   * @throws CheckoutStepOutOfOrderException if the session is past the payment step
   * @throws NothingToPayException if the total is not positive
   */
  public void submitPayment(final PaymentSelection payment) {
    ensurePaymentStepOpen();
    ensureSomethingToPay();

    this.paymentSelection = payment;

    // Advance to next step if currently at payment step
    if (currentStep == CheckoutStep.PAYMENT) {
      this.currentStep = CheckoutStep.REVIEW;
    }
  }

  /** The payment step takes a choice: the session takes changes, buyer and delivery are given. */
  private void ensurePaymentStepOpen() {
    ensureModifiable();
    ensureStepCompleted(CheckoutStep.BUYER_INFO);
    ensureStepCompleted(CheckoutStep.DELIVERY);
    ensureAtOrBeforeStep(CheckoutStep.PAYMENT);
  }

  /** A way to pay is chosen only for a session that has something to pay. */
  private void ensureSomethingToPay() {
    if (!totals.total().isPositive()) {
      throw new NothingToPayException(this.id, totals.total());
    }
  }

  /**
   * Records that the chosen provider authorized the reviewed total under a payment reference.
   *
   * <p>Raises a {@link PaymentSubmitted} domain event.
   *
   * @param paymentReference the payment reference the provider answered with
   * @param authorizedTotal the total the provider was asked for — the one the review showed
   * @throws CheckoutNotModifiableException if the session no longer takes changes
   * @throws CheckoutStepNotCompletedException if a step is still missing its data
   * @throws CheckoutStepOutOfOrderException if the session does not stand at the review step
   */
  public void authorizePayment(final String paymentReference, final Money authorizedTotal) {
    ensureModifiable();
    ensureAllStepsCompleted();
    authorizePayment(
        PaymentSelection.authorized(
            paymentSelection.providerId(), paymentReference, authorizedTotal));
  }

  /**
   * Records the payment a provider authorized: only the provider the customer chose may have.
   *
   * <p>Raises a {@link PaymentSubmitted} domain event.
   *
   * @param authorized the authorization — the provider that gave it, its payment reference and the
   *     total it was asked for
   * @throws CheckoutNotModifiableException if the session no longer takes changes
   * @throws CheckoutStepNotCompletedException if a step is still missing its data
   * @throws CheckoutStepOutOfOrderException if the session does not stand at the review step
   * @throws PaymentDiffersFromReviewException if a provider other than the chosen one authorized it
   * @throws IllegalArgumentException if the authorization carries no payment reference or no
   *     authorized amount
   */
  public void authorizePayment(final PaymentSelection authorized) {
    if (!authorized.hasReference() || authorized.authorizedAmount() == null) {
      throw new IllegalArgumentException(
          "An authorized payment needs a payment reference and an authorized amount");
    }
    ensureModifiable();
    ensureAllStepsCompleted();
    if (currentStep != CheckoutStep.REVIEW) {
      throw new CheckoutStepOutOfOrderException(this.id, CheckoutStep.REVIEW, currentStep);
    }
    if (!authorized.providerId().equals(paymentSelection.providerId())) {
      throw PaymentDiffersFromReviewException.anotherProvider(
          this.id, authorized.providerId(), paymentSelection.providerId());
    }

    this.paymentSelection = authorized;

    registerEvent(PaymentSubmitted.now(this.id, paymentSelection));
  }

  /**
   * Asserts that this session may be confirmed with that verdict right now.
   *
   * <p>The same preconditions {@link #confirm(CheckoutValidationResult, Map, ReviewFingerprint)}
   * enforces before it compares the review, without changing anything: a caller that is about to
   * reserve the order's stock asks this first, so a session that would be refused afterwards never
   * holds stock.
   *
   * @param validationResult the checkout pricing's verdict on the current line items
   * @throws CheckoutNotModifiableException if the session no longer takes changes
   * @throws CheckoutStepNotCompletedException if a step is still missing its data
   * @throws CheckoutStepOutOfOrderException if the session does not stand at the review step
   * @throws CheckoutValidationException if a line item no longer passes validation
   */
  public void assertReadyForConfirmation(final CheckoutValidationResult validationResult) {
    ensureModifiable();
    ensureAllStepsCompleted();

    if (currentStep != CheckoutStep.REVIEW) {
      throw new CheckoutStepOutOfOrderException(this.id, CheckoutStep.REVIEW, currentStep);
    }

    if (!validationResult.isValid()) {
      throw new CheckoutValidationException(validationResult);
    }
  }

  /**
   * Confirms the order the customer was shown in the review: its lines at today's prices and its
   * total.
   *
   * <p>The verdict and today's prices are handed in by the use case: deciding them needs article
   * facts the session does not own, so the domain service works them out and the session refuses to
   * confirm an invalid one, or one that is no longer the review the customer was shown.
   *
   * <p>Raises a {@link CheckoutConfirmed} domain event on success.
   *
   * @param validationResult the checkout pricing's verdict on the current line items
   * @param todaysPrices today's unit price per product
   * @param shown the fingerprint of the review the customer was shown
   * @throws CheckoutNotModifiableException if the session no longer takes changes
   * @throws CheckoutStepNotCompletedException if a step is still missing its data
   * @throws CheckoutStepOutOfOrderException if the session does not stand at the review step
   * @throws CheckoutValidationException if a line item no longer passes validation
   * @throws OrderChangedSinceReviewException if the review has changed since
   * @throws PaymentDiffersFromReviewException if the payment was authorized for an amount other
   *     than the review's total
   */
  public void confirm(
      final CheckoutValidationResult validationResult,
      final Map<ProductId, Money> todaysPrices,
      final ReviewFingerprint shown) {
    assertReadyForConfirmation(validationResult);
    final CheckoutReview review = assertReviewed(todaysPrices, shown);
    final Money reviewedTotal = review.totals().total();
    final Money authorized = paymentSelection.authorizedAmount();
    if (authorized != null && !authorized.equals(reviewedTotal)) {
      throw PaymentDiffersFromReviewException.anotherAmount(this.id, authorized, reviewedTotal);
    }

    this.lineItems.clear();
    this.lineItems.addAll(review.lineItems());
    this.totals = review.totals();
    this.status = CheckoutSessionStatus.CONFIRMED;
    this.currentStep = CheckoutStep.CONFIRMATION;

    registerEvent(
        CheckoutConfirmed.now(
            this.id, this.cartId, this.customerId, this.totals.total(), this.lineItems));
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
      case REVIEW -> isOrderPlaced();
      case CONFIRMATION -> status == CheckoutSessionStatus.COMPLETED;
    };
  }

  /**
   * Checks if an order was placed with this session: it was confirmed, and may have been completed
   * since.
   *
   * @return true if status is CONFIRMED or COMPLETED
   */
  public boolean isOrderPlaced() {
    return status == CheckoutSessionStatus.CONFIRMED || status == CheckoutSessionStatus.COMPLETED;
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
      throw new CheckoutNotModifiableException(this.id, status);
    }
  }

  private void ensureStepCompleted(final CheckoutStep step) {
    if (!isStepCompleted(step)) {
      throw new CheckoutStepNotCompletedException(this.id, step);
    }
  }

  private void ensureAtOrBeforeStep(final CheckoutStep step) {
    // Allow modifying current step or going back to modify previous steps
    // Cannot skip forward (e.g., submit payment before delivery)
    if (currentStep.isBefore(step)) {
      throw new CheckoutStepOutOfOrderException(this.id, step, currentStep);
    }
  }

  private void ensureAllStepsCompleted() {
    ensureStepCompleted(CheckoutStep.BUYER_INFO);
    ensureStepCompleted(CheckoutStep.DELIVERY);
    ensureStepCompleted(CheckoutStep.PAYMENT);
  }
}
