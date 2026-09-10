package dev.domaincentric.sample.ecommerce.inventory.domain.specification;

import dev.domaincentric.sample.ecommerce.inventory.domain.model.StockLevel;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.specification.SpecificationVisitor;

/**
 * Visitor for translating stock specifications to adapter-specific forms.
 *
 * <p>A persistence adapter implements this interface to translate a domain-level stock
 * specification into a query predicate, so the filtering happens where the data is.
 */
public interface StockLevelSpecificationVisitor<R> extends SpecificationVisitor<StockLevel, R> {
  R visit(AvailableQuantityBelow spec);
}
