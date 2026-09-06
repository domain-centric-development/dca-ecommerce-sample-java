package dev.domaincentric.sample.ecommerce.checkout.application.session.getcheckoutsession;

import dev.domaincentric.sample.ecommerce.checkout.application.shared.CheckoutSessionRepository;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutStep;
import dev.domaincentric.sample.ecommerce.checkout.domain.readmodel.CheckoutCartSnapshot;
import dev.domaincentric.sample.ecommerce.checkout.domain.service.CheckoutStepValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case for retrieving a checkout session by ID.
 *
 * <p>This use case loads all session data for display, including line items, totals, buyer info,
 * delivery, and payment information. When the query names the step the caller wants to open, the
 * {@link CheckoutStepValidator} decides whether that step is accessible and the decision travels in
 * the result — the adapter formats it, it does not compute it.
 *
 * <p><b>Hexagonal Architecture:</b> This class implements the {@link GetCheckoutSessionInputPort}
 * interface, which is a primary/driving port in the application layer.
 */
@Service
@Transactional(readOnly = true)
public class GetCheckoutSessionUseCase implements GetCheckoutSessionInputPort {

  private final CheckoutSessionRepository checkoutSessionRepository;
  private final CheckoutStepValidator checkoutStepValidator;

  public GetCheckoutSessionUseCase(
      final CheckoutSessionRepository checkoutSessionRepository,
      final CheckoutStepValidator checkoutStepValidator) {
    this.checkoutSessionRepository = checkoutSessionRepository;
    this.checkoutStepValidator = checkoutStepValidator;
  }

  @Override
  public GetCheckoutSessionResult execute(final GetCheckoutSessionQuery query) {
    final CheckoutStep requestedStep = query.requestedStep();
    return checkoutSessionRepository
        .findById(query.sessionId())
        .map(CheckoutCartSnapshot::from)
        .map(
            snapshot ->
                requestedStep == null
                    ? GetCheckoutSessionResult.found(snapshot)
                    : GetCheckoutSessionResult.found(
                        snapshot, checkoutStepValidator.accessTo(snapshot, requestedStep)))
        .orElseGet(
            () ->
                requestedStep == null
                    ? GetCheckoutSessionResult.notFound()
                    : GetCheckoutSessionResult.notFoundForStep());
  }
}
