package dev.domaincentric.sample.ecommerce.pricing.application.changeprice;

import dev.domaincentric.dca.buildingblocks.hexagonal.port.out.DomainEventPublisher;
import dev.domaincentric.sample.ecommerce.pricing.application.shared.ProductPriceRepository;
import dev.domaincentric.sample.ecommerce.pricing.domain.model.ProductPrice;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Money;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Price;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.ProductId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case that changes a product's price: the current price is replaced by the new amount, in the
 * currency of the price it replaces, and {@code PriceChanged} is published.
 */
@Service
@Transactional
public class ChangePriceUseCase implements ChangePriceInputPort {

  private final ProductPriceRepository productPriceRepository;
  private final DomainEventPublisher eventPublisher;

  public ChangePriceUseCase(
      final ProductPriceRepository productPriceRepository,
      final DomainEventPublisher eventPublisher) {
    this.productPriceRepository = productPriceRepository;
    this.eventPublisher = eventPublisher;
  }

  @Override
  public ChangePriceResult execute(final ChangePriceCommand command) {
    final ProductId productId = ProductId.of(command.productId());
    final ProductPrice productPrice =
        productPriceRepository
            .findByProductId(productId)
            .orElseThrow(() -> new ProductPriceNotFoundException(productId));

    final Money current = productPrice.currentPrice().value();
    productPrice.updatePrice(Price.of(new Money(command.priceAmount(), current.currency())));

    productPriceRepository.save(productPrice);
    eventPublisher.publishAndClearEvents(productPrice);

    final Money changed = productPrice.currentPrice().value();
    return new ChangePriceResult(
        productId.value(),
        changed.amount(),
        changed.currency().getCurrencyCode(),
        productPrice.effectiveFrom());
  }
}
