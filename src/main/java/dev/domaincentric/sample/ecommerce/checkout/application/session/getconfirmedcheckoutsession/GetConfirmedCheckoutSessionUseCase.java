package dev.domaincentric.sample.ecommerce.checkout.application.getconfirmedcheckoutsession;

import dev.domaincentric.sample.ecommerce.checkout.application.shared.CheckoutSessionRepository;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CustomerId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case for getting a confirmed or completed checkout session for a customer.
 *
 * <p>This use case retrieves a confirmed or completed checkout session for displaying the
 * confirmation/thank you page after order confirmation.
 *
 * <p><b>Hexagonal Architecture:</b> This class implements the {@link
 * GetConfirmedCheckoutSessionInputPort} interface, which is a primary/driving port in the
 * application layer.
 */
@Service
@Transactional(readOnly = true)
public class GetConfirmedCheckoutSessionUseCase implements GetConfirmedCheckoutSessionInputPort {

  private final CheckoutSessionRepository checkoutSessionRepository;

  public GetConfirmedCheckoutSessionUseCase(
      final CheckoutSessionRepository checkoutSessionRepository) {
    this.checkoutSessionRepository = checkoutSessionRepository;
  }

  @Override
  public GetConfirmedCheckoutSessionResult execute(final GetConfirmedCheckoutSessionQuery query) {
    final CustomerId customerId = CustomerId.of(query.customerId());

    return checkoutSessionRepository
        .findConfirmedOrCompletedByCustomerId(customerId)
        .map(
            session ->
                GetConfirmedCheckoutSessionResult.of(
                    session.id().value(), session.customerId().value()))
        .orElseGet(GetConfirmedCheckoutSessionResult::notFound);
  }
}
