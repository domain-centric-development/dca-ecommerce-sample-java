package dev.domaincentric.sample.ecommerce.cart.adapter.outgoing.persistence.jpa;

import dev.domaincentric.sample.ecommerce.cart.adapter.outgoing.persistence.CartStatusColumn;
import dev.domaincentric.sample.ecommerce.cart.domain.model.CartStatus;
import dev.domaincentric.sample.ecommerce.cart.domain.model.ShoppingCart;
import dev.domaincentric.sample.ecommerce.cart.domain.specification.*;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.specification.AndSpecification;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.specification.CompositeSpecification;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.specification.NotSpecification;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.specification.OrSpecification;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import java.math.BigDecimal;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

/**
 * Translates domain-level cart specifications into Spring Data JPA Specifications to allow
 * database-side filtering (predicate pushdown).
 */
@Component
public class CartSpecToJpa implements CartSpecificationVisitor<Specification<CartEntity>> {

  // --- Leaf specs

  @Override
  public Specification<CartEntity> visit(ActiveCart spec) {
    return (root, query, cb) ->
        cb.equal(root.get("status"), CartStatusColumn.valueOf(CartStatus.ACTIVE));
  }

  @Override
  public Specification<CartEntity> visit(LastUpdatedBefore spec) {
    return (root, query, cb) -> cb.lessThan(root.get("updatedAt"), spec.threshold());
  }

  @Override
  public Specification<CartEntity> visit(HasMinTotal spec) {
    return (root, query, cb) -> {
      // Join items and aggregate sum(price_amount * quantity) filtered by currency
      Join<CartEntity, CartItemEntity> items = root.join("items", JoinType.LEFT);

      // Convert quantity to BigDecimal and multiply
      Expression<BigDecimal> quantityAsBig = cb.toBigDecimal(items.get("quantity"));
      Expression<BigDecimal> lineTotal = cb.prod(items.get("priceAmount"), quantityAsBig);

      // Only sum items that match the target currency to avoid mixing currencies
      query.groupBy(root.get("id"));
      query.having(
          cb.greaterThanOrEqualTo(
              cb.coalesce(
                  cb.sum(
                      cb.<BigDecimal>selectCase()
                          .when(
                              cb.equal(
                                  items.get("priceCurrency"),
                                  spec.minimum().currency().getCurrencyCode()),
                              lineTotal)
                          .otherwise(cb.literal(BigDecimal.ZERO))),
                  BigDecimal.ZERO),
              spec.minimum().amount()));

      // Some JPA providers require returning a dummy predicate when only HAVING is used; use TRUE =
      // TRUE
      return cb.conjunction();
    };
  }

  // Additions for new leaf specs
  @Override
  public Specification<CartEntity> visit(HasAnyAvailableItem spec) {
    return (root, query, cb) -> {
      // Minimal pushdown: require at least one item with quantity > 0
      // (No join to products here; add when product read-model is present)
      Join<CartEntity, CartItemEntity> items = root.join("items", JoinType.INNER);
      query.distinct(true);
      return cb.greaterThan(items.get("quantity"), 0);
    };
  }

  @Override
  public Specification<CartEntity> visit(CustomerAllowsMarketing spec) {
    // No customer read-model entity available yet; keep as no-op to preserve semantics.
    // TODO: Join customers table (or subquery) to filter by allows_marketing flag when available.
    return (root, query, cb) -> cb.conjunction();
  }

  // --- Combinators from shared visitor

  @Override
  public Specification<CartEntity> visit(AndSpecification<ShoppingCart> spec) {
    Specification<CartEntity> left = dispatch(spec.left());
    Specification<CartEntity> right = dispatch(spec.right());
    return left.and(right);
  }

  @Override
  public Specification<CartEntity> visit(OrSpecification<ShoppingCart> spec) {
    Specification<CartEntity> left = dispatch(spec.left());
    Specification<CartEntity> right = dispatch(spec.right());
    return left.or(right);
  }

  @Override
  public Specification<CartEntity> visit(NotSpecification<ShoppingCart> spec) {
    Specification<CartEntity> inner = dispatch(spec.inner());
    return Specification.not(inner);
  }

  private Specification<CartEntity> dispatch(CompositeSpecification<ShoppingCart> s) {
    return s.accept(this);
  }
}
