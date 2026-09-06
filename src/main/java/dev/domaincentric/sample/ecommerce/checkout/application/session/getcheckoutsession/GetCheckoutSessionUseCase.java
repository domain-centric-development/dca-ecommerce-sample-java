package dev.domaincentric.sample.ecommerce.checkout.application.getcheckoutsession;

import dev.domaincentric.sample.ecommerce.checkout.application.shared.CheckoutSessionRepository;
import dev.domaincentric.sample.ecommerce.checkout.domain.readmodel.CheckoutCartSnapshot;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case for retrieving a checkout session by ID.
 *
 * <p>This use case loads all session data for display, including line items, totals, buyer info,
 * delivery, and payment information.
 *
 * <p><b>Hexagonal Architecture:</b> This class implements the {@link GetCheckoutSessionInputPort}
 * interface, which is a primary/driving port in the application layer.
 */
@Service
@Transactional(readOnly = true)
public class GetCheckoutSessionUseCase implements GetCheckoutSessionInputPort {

  private final CheckoutSessionRepository checkoutSessionRepository;

  public GetCheckoutSessionUseCase(final CheckoutSessionRepository checkoutSessionRepository) {
    this.checkoutSessionRepository = checkoutSessionRepository;
  }

  @Override
  public GetCheckoutSessionResult execute(final GetCheckoutSessionQuery query) {
    return checkoutSessionRepository
        .findById(query.sessionId())
        .map(CheckoutCartSnapshot::from)
        .map(GetCheckoutSessionResult::found)
        .orElseGet(GetCheckoutSessionResult::notFound);
  }
}
