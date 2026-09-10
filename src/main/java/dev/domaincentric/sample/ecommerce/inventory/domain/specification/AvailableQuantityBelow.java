package dev.domaincentric.sample.ecommerce.inventory.domain.specification;

import dev.domaincentric.sample.ecommerce.inventory.domain.model.StockLevel;
import dev.domaincentric.sample.ecommerce.inventory.domain.model.StockQuantity;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.specification.AndSpecification;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.specification.SpecificationVisitor;
import java.util.Objects;

/** The available quantity of a stock level is below the given threshold. */
public record AvailableQuantityBelow(StockQuantity threshold) implements StockLevelSpecification {
  public AvailableQuantityBelow {
    Objects.requireNonNull(threshold, "threshold must not be null");
  }

  /**
   * Compares the quantity the warehouse holds — reservations do not enter the comparison, and the
   * threshold itself is not low.
   */
  @Override
  public boolean isSatisfiedBy(StockLevel candidate) {
    return candidate.availableQuantity().value() < threshold.value();
  }

  @Override
  public <R> R accept(SpecificationVisitor<StockLevel, R> visitor) {
    if (visitor instanceof StockLevelSpecificationVisitor<?> v) {
      @SuppressWarnings("unchecked")
      final StockLevelSpecificationVisitor<R> sv = (StockLevelSpecificationVisitor<R>) v;
      return sv.visit(this);
    }
    return visitor.visit(new AndSpecification<>(this, this));
  }
}
