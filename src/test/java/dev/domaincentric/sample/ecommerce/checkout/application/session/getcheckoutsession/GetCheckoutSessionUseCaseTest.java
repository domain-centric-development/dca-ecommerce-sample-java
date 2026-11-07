package dev.domaincentric.sample.ecommerce.checkout.application.session.getcheckoutsession;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import dev.domaincentric.sample.ecommerce.checkout.adapter.outgoing.persistence.InMemoryCheckoutSessionRepository;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutSessionId;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutStep;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CustomerId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * A session that is not there is the one step decision the snapshot cannot take — there is no
 * snapshot to ask. The use case answers it, and these cases live here because of that.
 */
@DisplayName("GetCheckoutSessionUseCase: no session for this customer")
class GetCheckoutSessionUseCaseTest {

  private final InMemoryCheckoutSessionRepository sessions =
      new InMemoryCheckoutSessionRepository();
  private final GetCheckoutSessionUseCase useCase = new GetCheckoutSessionUseCase(sessions);

  @Test
  @DisplayName("a requested step finds no checkout the customer does not have")
  void aRequestedStepFindsNoCheckout() {
    final CheckoutSessionId sessionId = CheckoutSessionId.generate();
    final GetCheckoutSessionResult result =
        useCase.execute(
            new GetCheckoutSessionQuery(
                sessionId, CustomerId.of("someone-else"), CheckoutStep.BUYER_INFO));

    assertEquals(new GetCheckoutSessionResult.CheckoutSessionNotFound(sessionId), result);
  }

  @Test
  @DisplayName("every step finds no checkout the customer does not have")
  void everyStepFindsNoCheckout() {
    for (final CheckoutStep step : CheckoutStep.values()) {
      final GetCheckoutSessionResult result =
          useCase.execute(
              new GetCheckoutSessionQuery(
                  CheckoutSessionId.generate(), CustomerId.of("someone-else"), step));

      assertInstanceOf(
          GetCheckoutSessionResult.CheckoutSessionNotFound.class,
          result,
          "Should not be found for step: " + step);
    }
  }

  @Test
  @DisplayName("without a requested step no checkout is found either")
  void withoutARequestedStepNoCheckoutIsFound() {
    final GetCheckoutSessionResult result =
        useCase.execute(
            new GetCheckoutSessionQuery(
                CheckoutSessionId.generate(), CustomerId.of("someone-else"), null));

    assertInstanceOf(GetCheckoutSessionResult.CheckoutSessionNotFound.class, result);
  }
}
