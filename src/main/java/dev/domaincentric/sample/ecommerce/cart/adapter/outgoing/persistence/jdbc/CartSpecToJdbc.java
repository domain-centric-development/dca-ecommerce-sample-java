package dev.domaincentric.sample.ecommerce.cart.adapter.outgoing.persistence.jdbc;

import dev.domaincentric.sample.ecommerce.cart.adapter.outgoing.persistence.CartStatusColumn;
import dev.domaincentric.sample.ecommerce.cart.domain.model.CartStatus;
import dev.domaincentric.sample.ecommerce.cart.domain.model.ShoppingCart;
import dev.domaincentric.sample.ecommerce.cart.domain.specification.*;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.specification.AndSpecification;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.specification.CompositeSpecification;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.specification.NotSpecification;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.specification.OrSpecification;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Translates domain cart specifications into JDBC WHERE fragments with bind parameters.
 *
 * <p>The resulting SQL snippets are intended to be appended to a base query that selects from the
 * {@code carts} table using the alias {@code c}. Item-related predicates use correlated subqueries
 * against {@code cart_items}.
 */
@Component
public class CartSpecToJdbc implements CartSpecificationVisitor<CartSpecToJdbc.JdbcPredicate> {

  public record JdbcPredicate(String sql, List<Object> params) {
    public static JdbcPredicate alwaysTrue() {
      return new JdbcPredicate("1=1", List.of());
    }
  }

  // ---- Leaf specs

  @Override
  public JdbcPredicate visit(ActiveCart spec) {
    return new JdbcPredicate("c.status = ?", List.of(CartStatusColumn.valueOf(CartStatus.ACTIVE)));
  }

  @Override
  public JdbcPredicate visit(LastUpdatedBefore spec) {
    return new JdbcPredicate("c.updated_at < ?", List.of(spec.threshold()));
  }

  @Override
  public JdbcPredicate visit(HasMinTotal spec) {
    // Sum over items of this cart filtered by currency
    final String sql =
        "(SELECT COALESCE(SUM(ci.price_amount * ci.quantity), 0) FROM cart_items ci "
            + "WHERE ci.cart_id = c.id AND ci.price_currency = ?) >= ?";
    final List<Object> params = new ArrayList<>();
    params.add(spec.minimum().currency().getCurrencyCode());
    params.add(spec.minimum().amount());
    return new JdbcPredicate(sql, params);
  }

  @Override
  public JdbcPredicate visit(HasAnyAvailableItem spec) {
    final String sql =
        "EXISTS (SELECT 1 FROM cart_items ci WHERE ci.cart_id = c.id AND ci.quantity > 0)";
    return new JdbcPredicate(sql, List.of());
  }

  @Override
  public JdbcPredicate visit(CustomerAllowsMarketing spec) {
    // No customer read-model currently; treat as no-op
    return JdbcPredicate.alwaysTrue();
  }

  // ---- Combinators

  @Override
  public JdbcPredicate visit(AndSpecification<ShoppingCart> spec) {
    final JdbcPredicate l = dispatch(spec.left());
    final JdbcPredicate r = dispatch(spec.right());
    return combine("AND", l, r);
  }

  @Override
  public JdbcPredicate visit(OrSpecification<ShoppingCart> spec) {
    final JdbcPredicate l = dispatch(spec.left());
    final JdbcPredicate r = dispatch(spec.right());
    return combine("OR", l, r);
  }

  @Override
  public JdbcPredicate visit(NotSpecification<ShoppingCart> spec) {
    final JdbcPredicate inner = dispatch(spec.inner());
    final String sql = "NOT (" + inner.sql + ")";
    return new JdbcPredicate(sql, inner.params);
  }

  private JdbcPredicate combine(String op, JdbcPredicate l, JdbcPredicate r) {
    final List<Object> params = new ArrayList<>(l.params);
    params.addAll(r.params);
    return new JdbcPredicate("(" + l.sql + ") " + op + " (" + r.sql + ")", params);
  }

  private JdbcPredicate dispatch(CompositeSpecification<ShoppingCart> s) {
    return s.accept(this);
  }
}
