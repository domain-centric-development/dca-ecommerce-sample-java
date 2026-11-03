package dev.domaincentric.sample.ecommerce.cart.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Money;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Price;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.ProductId;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("EnrichedCart subtotal")
class EnrichedCartSubtotalTest {

  @Test
  @DisplayName("the current subtotal counts only the lines whose article is known")
  void theCurrentSubtotalLeavesOutALineWithoutArticle() {
    final ProductId known = ProductId.generate();
    final EnrichedCartItem twoKnown =
        new EnrichedCartItem(
            CartItemId.generate(),
            known,
            Quantity.of(2),
            Price.of(Money.euro(39.99)),
            CartArticle.of(known, "Article", Money.euro(39.99), 5, true, ""));
    final EnrichedCartItem lost =
        new EnrichedCartItem(
            CartItemId.generate(),
            ProductId.generate(),
            Quantity.of(1),
            Price.of(Money.euro(54.99)),
            Optional.empty(),
            "snapshot");
    final EnrichedCart cart =
        EnrichedCart.of(
            CartId.generate(),
            CustomerId.of("customer"),
            List.of(twoKnown, lost),
            CartStatus.ACTIVE);

    assertEquals(Money.euro(79.98), cart.calculateCurrentSubtotal());
  }
}
