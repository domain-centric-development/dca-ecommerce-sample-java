package dev.domaincentric.sample.ecommerce.pricing.application.setproductprice;

import dev.domaincentric.dca.buildingblocks.hexagonal.port.out.DomainEventPublisher;
import dev.domaincentric.sample.ecommerce.pricing.application.shared.ProductPriceRepository;
import dev.domaincentric.sample.ecommerce.pricing.domain.model.ProductPrice;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Money;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.ProductId;
import java.util.Currency;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case for setting or updating a product's price.
 *
 * <p>This use case orchestrates price management by:
 *
 * <ol>
 *   <li>Looking up existing price for the product
 *   <li>Creating a new price or updating the existing one
 *   <li>Persisting the changes
 *   <li>Publishing domain events
 * </ol>
 *
 * <p><b>Hexagonal Architecture:</b> This class implements the {@link SetProductPriceInputPort}
 * interface, which is a primary/driving port in the application layer.
 */
@Service
@Transactional
public class SetProductPriceUseCase implements SetProductPriceInputPort {

  private final ProductPriceRepository productPriceRepository;
  private final DomainEventPublisher eventPublisher;

  public SetProductPriceUseCase(
      final ProductPriceRepository productPriceRepository,
      final DomainEventPublisher eventPublisher) {
    this.productPriceRepository = productPriceRepository;
    this.eventPublisher = eventPublisher;
  }

  @Override
  public SetProductPriceResult execute(final SetProductPriceCommand command) {
    final ProductId productId = ProductId.of(command.productId());
    final dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Price newPrice =
        dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Price.of(
            new Money(command.priceAmount(), Currency.getInstance(command.priceCurrency())));

    // Check if price exists for this product
    final var existingPrice = productPriceRepository.findByProductId(productId);

    final ProductPrice productPrice;
    final boolean created;

    if (existingPrice.isPresent()) {
      // Update existing price
      productPrice = existingPrice.get();
      productPrice.updatePrice(newPrice);
      created = false;
    } else {
      // Create new price
      productPrice = ProductPrice.create(productId, newPrice);
      created = true;
    }

    // Persist
    productPriceRepository.save(productPrice);

    // Publish domain events
    eventPublisher.publishAndClearEvents(productPrice);

    // Map to result
    return new SetProductPriceResult(
        productPrice.id().value(),
        productPrice.productId().value(),
        productPrice.currentPrice().value().amount(),
        productPrice.currentPrice().value().currency().getCurrencyCode(),
        productPrice.effectiveFrom(),
        created);
  }
}
