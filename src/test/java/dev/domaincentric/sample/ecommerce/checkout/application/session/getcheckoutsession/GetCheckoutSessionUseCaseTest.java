package dev.domaincentric.sample.ecommerce.checkout.application.session.getcheckoutsession;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
  @DisplayName("a requested step sends the customer back to the cart")
  void aRequestedStepSendsTheCustomerBackToTheCart() {
    final GetCheckoutSessionResult result =
        useCase.execute(
            new GetCheckoutSessionQuery(
                CheckoutSessionId.generate(),
                CustomerId.of("someone-else"),
                CheckoutStep.BUYER_INFO));

    assertFalse(result.found());
    assertNotNull(result.stepAccess());
    assertTrue(result.stepAccess().isBackToCart());
  }

  @Test
  @DisplayName("every step sends the customer back to the cart")
  void everyStepSendsTheCustomerBackToTheCart() {
    for (final CheckoutStep step : CheckoutStep.values()) {
      final GetCheckoutSessionResult result =
          useCase.execute(
              new GetCheckoutSessionQuery(
                  CheckoutSessionId.generate(), CustomerId.of("someone-else"), step));

      assertTrue(result.stepAccess().isBackToCart(), "Should redirect for step: " + step);
    }
  }

  @Test
  @DisplayName("without a requested step there is no access decision to take")
  void withoutARequestedStepThereIsNoAccessDecisionToTake() {
    final GetCheckoutSessionResult result =
        useCase.execute(
            new GetCheckoutSessionQuery(
                CheckoutSessionId.generate(), CustomerId.of("someone-else"), null));

    assertFalse(result.found());
    assertNull(result.stepAccess());
    assertEquals(null, result.currentStep());
  }
}
