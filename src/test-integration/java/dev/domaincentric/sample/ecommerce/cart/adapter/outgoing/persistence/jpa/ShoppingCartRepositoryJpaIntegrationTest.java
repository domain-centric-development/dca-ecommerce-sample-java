package dev.domaincentric.sample.ecommerce.cart.adapter.outgoing.persistence.jpa;

import static org.junit.jupiter.api.Assertions.*;

import dev.domaincentric.sample.ecommerce.cart.application.shared.ShoppingCartRepository;
import dev.domaincentric.sample.ecommerce.cart.domain.model.CartId;
import dev.domaincentric.sample.ecommerce.cart.domain.model.CustomerId;
import dev.domaincentric.sample.ecommerce.cart.domain.model.Quantity;
import dev.domaincentric.sample.ecommerce.cart.domain.model.ShoppingCart;
import dev.domaincentric.sample.ecommerce.cart.domain.specification.ActiveCart;
import dev.domaincentric.sample.ecommerce.cart.domain.specification.HasMinTotal;
import dev.domaincentric.sample.ecommerce.infrastructure.EcommerceSampleApplication;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Money;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.PageResult;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.PagingRequest;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Price;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.ProductId;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(classes = EcommerceSampleApplication.class)
@Transactional
class ShoppingCartRepositoryJpaIntegrationTest {

  @Autowired private ShoppingCartRepository shoppingCartRepository;

  @Test
  void save_thenFindById_andFindActiveByCustomer_shouldRoundTrip() {
    // given
    CustomerId customerId = CustomerId.of("it-customer-1");
    CartId cartId = CartId.generate();
    ShoppingCart cart = new ShoppingCart(cartId, customerId);

    // when
    shoppingCartRepository.save(cart);

    // then: find by id works
    Optional<ShoppingCart> byId = shoppingCartRepository.findById(cartId);
    assertTrue(byId.isPresent(), "Expected cart to be found by id after save");
    assertEquals(cartId.value(), byId.get().id().value());

    // and: find active cart by customer works
    Optional<ShoppingCart> active = shoppingCartRepository.findActiveCartByCustomerId(customerId);
    assertTrue(active.isPresent(), "Expected active cart for customer after save");
    assertEquals(cartId.value(), active.get().id().value());
  }

  @Test
  void findBy_spec_withMinTotal_andActive_paginatesAndFilters() {
    // given: two open carts, only one meeting min total. They belong to different customers
    // because a customer has at most one open cart and the store claims that (ADR-045); the
    // specification under test filters by status and total, not by customer.
    CustomerId oneCustomer = CustomerId.of("it-customer-2-a");
    CustomerId anotherCustomer = CustomerId.of("it-customer-2-b");

    ShoppingCart small = new ShoppingCart(CartId.generate(), oneCustomer);
    small.addItem(ProductId.of("P1"), Quantity.of(1), Price.of(Money.euro(10.00)));

    ShoppingCart big = new ShoppingCart(CartId.generate(), anotherCustomer);
    big.addItem(ProductId.of("P2"), Quantity.of(3), Price.of(Money.euro(25.00))); // total 75 EUR

    shoppingCartRepository.save(small);
    shoppingCartRepository.save(big);

    // when: compose spec and query
    var spec = new ActiveCart().and(new HasMinTotal(Money.euro(50.00)));

    PageResult<ShoppingCart> page = shoppingCartRepository.findBy(spec, PagingRequest.of(0, 10));

    // then: only the big cart should be returned
    assertEquals(1, page.totalElements(), "Expected exactly one cart meeting the spec");
    assertEquals(1, page.content().size(), "Expected a single cart in the first page");
    assertEquals(big.id().value(), page.content().get(0).id().value());
  }
}
