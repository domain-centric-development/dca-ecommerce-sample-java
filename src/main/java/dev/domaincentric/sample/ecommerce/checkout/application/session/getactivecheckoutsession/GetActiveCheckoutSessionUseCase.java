package dev.domaincentric.sample.ecommerce.checkout.application.getactivecheckoutsession;

import dev.domaincentric.sample.ecommerce.checkout.application.shared.CheckoutSessionRepository;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CustomerId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case for getting an active checkout session for a customer.
 *
 * <p>This use case retrieves the active checkout session for a customer based on their customer ID
 * (derived from JWT identity).
 *
 * <p><b>Hexagonal Architecture:</b> This class implements the {@link
 * GetActiveCheckoutSessionInputPort} interface, which is a primary/driving port in the application
 * layer.
 */
@Service
@Transactional(readOnly = true)
public class GetActiveCheckoutSessionUseCase implements GetActiveCheckoutSessionInputPort {

  private final CheckoutSessionRepository checkoutSessionRepository;

  public GetActiveCheckoutSessionUseCase(
      final CheckoutSessionRepository checkoutSessionRepository) {
    this.checkoutSessionRepository = checkoutSessionRepository;
  }

  @Override
  public GetActiveCheckoutSessionResult execute(final GetActiveCheckoutSessionQuery query) {
    final CustomerId customerId = CustomerId.of(query.customerId());

    return checkoutSessionRepository
        .findActiveByCustomerId(customerId)
        .map(
            session ->
                GetActiveCheckoutSessionResult.of(
                    session.id().value(), session.customerId().value()))
        .orElseGet(GetActiveCheckoutSessionResult::notFound);
  }
}
