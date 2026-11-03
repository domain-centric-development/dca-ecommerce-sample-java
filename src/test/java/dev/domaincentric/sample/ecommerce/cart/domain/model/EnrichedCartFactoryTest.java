package dev.domaincentric.sample.ecommerce.cart.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Money;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Price;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.ProductId;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("EnrichedCartFactory")
class EnrichedCartFactoryTest {

  @Test
  @DisplayName("a line whose product has no article data is kept with its id, product and quantity")
  void aLineWithoutArticleDataIsKept() {
    final ShoppingCart cart = ShoppingCart.create(CartId.generate(), CustomerId.of("customer"));
    final ProductId lost = ProductId.generate();
    cart.addItem(lost, Quantity.of(3), Price.of(Money.euro(10.0)));
    final CartItem line = cart.items().getFirst();

    final EnrichedCart enriched = new EnrichedCartFactory().create(cart, Map.of());

    assertEquals(1, enriched.items().size());
    final EnrichedCartItem kept = enriched.items().getFirst();
    assertEquals(line.id(), kept.cartItemId());
    assertEquals(lost, kept.productId());
    assertEquals(Quantity.of(3), kept.quantity());
    assertTrue(kept.article().isEmpty());
  }
}
