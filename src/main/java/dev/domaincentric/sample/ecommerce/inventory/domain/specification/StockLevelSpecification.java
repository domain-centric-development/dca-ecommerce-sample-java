package dev.domaincentric.sample.ecommerce.inventory.domain.specification;

import dev.domaincentric.sample.ecommerce.inventory.domain.model.StockLevel;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.specification.CompositeSpecification;

/**
 * Marker interface for stock-related specifications.
 *
 * <p>Extends the generic {@link CompositeSpecification} for {@link StockLevel} so that adapters can
 * translate the specific leaf specifications without leaking a query technology into the domain.
 */
public sealed interface StockLevelSpecification extends CompositeSpecification<StockLevel>
    permits AvailableQuantityBelow {}
