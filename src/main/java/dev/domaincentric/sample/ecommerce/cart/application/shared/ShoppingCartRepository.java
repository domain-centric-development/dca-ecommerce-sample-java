package dev.domaincentric.sample.ecommerce.cart.application.shared;

import dev.domaincentric.dca.buildingblocks.hexagonal.port.out.Repository;
import dev.domaincentric.sample.ecommerce.cart.domain.model.CartId;
import dev.domaincentric.sample.ecommerce.cart.domain.model.CustomerId;
import dev.domaincentric.sample.ecommerce.cart.domain.model.ShoppingCart;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.PageResult;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.PagingRequest;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.specification.CompositeSpecification;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for ShoppingCart aggregate.
 *
 * <p>Provides collection-like access to ShoppingCart aggregates using domain language.
 * Implementation resides in the secondary adapter layer.
 *
 * <p>Extends the base {@link Repository} interface which provides common methods:
 *
 * <ul>
 *   <li>{@code findById(CartId)} - inherited from base interface
 *   <li>{@code save(ShoppingCart)} - inherited from base interface
 *   <li>{@code deleteById(CartId)} - inherited from base interface
 * </ul>
 */
public interface ShoppingCartRepository extends Repository<ShoppingCart, CartId> {

  /**
   * Finds all carts for a specific customer.
   *
   * @param customerId the customer ID
   * @return list of carts belonging to the customer
   */
  List<ShoppingCart> findByCustomerId(CustomerId customerId);

  /**
   * Finds the active cart for a customer.
   *
   * @param customerId the customer ID
   * @return the active cart if found, empty otherwise
   */
  /**
   * Finds one customer's cart by ID — empty when it does not exist <em>or</em> is not theirs.
   *
   * <p>Every use case that acts on a cart a caller named reaches for this rather than {@code
   * findById}: the two cases are indistinguishable on purpose, and a persistence adapter expresses
   * it as one predicate. The cart is an owned aggregate; {@code findById} stays for the system
   * paths that act on nobody's behalf and hold no owner to name.
   *
   * @param cartId the cart ID
   * @param customerId the customer the caller is acting as
   * @return the cart if it exists and belongs to that customer
   */
  Optional<ShoppingCart> findByIdForCustomer(CartId cartId, CustomerId customerId);

  Optional<ShoppingCart> findActiveCartByCustomerId(CustomerId customerId);

  /**
   * Retrieves all shopping carts.
   *
   * @return list of all carts
   */
  List<ShoppingCart> findAll();

  /**
   * Find carts matching the given specification using database-side filtering and pagination.
   *
   * <p>The specification is expressed in domain terms and translated by the persistence adapter
   * into native predicates (e.g., JPA criteria) to avoid loading and filtering in memory.
   *
   * <p>Default implementation falls back to in-memory filtering and manual paging, so secondary
   * adapters can opt-in to DB pushdown progressively.
   */
  default PageResult<ShoppingCart> findBy(
      CompositeSpecification<ShoppingCart> specification, PagingRequest pageQuery) {
    final List<ShoppingCart> filtered =
        findAll().stream().filter(specification::isSatisfiedBy).toList();
    final int start = (int) pageQuery.offset();
    final int end = Math.min(start + pageQuery.pageSize(), filtered.size());
    final List<ShoppingCart> content =
        start >= filtered.size() ? List.of() : filtered.subList(start, end);
    return new PageResult<>(content, filtered.size(), pageQuery.pageNumber(), pageQuery.pageSize());
  }
}
