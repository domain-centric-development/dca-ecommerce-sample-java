package dev.domaincentric.sample.ecommerce.pricing.api;

import dev.domaincentric.dca.buildingblocks.ddd.strategic.relationships.OpenHostService;
import dev.domaincentric.sample.ecommerce.pricing.application.getpricesforproducts.GetPricesForProductsInputPort;
import dev.domaincentric.sample.ecommerce.pricing.application.getpricesforproducts.GetPricesForProductsQuery;
import dev.domaincentric.sample.ecommerce.pricing.application.getpricesforproducts.GetPricesForProductsResult;
import dev.domaincentric.sample.ecommerce.pricing.application.setproductprice.SetProductPriceCommand;
import dev.domaincentric.sample.ecommerce.pricing.application.setproductprice.SetProductPriceInputPort;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Money;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.ProductId;
import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

/**
 * Open Host Service for Pricing.
 *
 * <p>An Open Host Service in {@code api/}: the in-process published contract of the Pricing
 * context. It delegates to use cases (input ports) and translates responses to its own DTOs.
 *
 * <p>Consuming contexts should NOT use this service directly in their use cases - they should
 * define their own output ports and implement adapters that delegate to this service.
 *
 * <p><b>Hexagonal Architecture:</b> Like an incoming adapter, this service calls input ports (use
 * cases), never output ports (repositories) directly.
 */
@OpenHostService(
    context = "Pricing",
    description = "Provides pricing information for other bounded contexts")
@Service("pricingContextOhs")
public class PricingService {

  private final GetPricesForProductsInputPort getPricesForProductsInputPort;
  private final SetProductPriceInputPort setProductPriceInputPort;

  public PricingService(
      GetPricesForProductsInputPort getPricesForProductsInputPort,
      SetProductPriceInputPort setProductPriceInputPort) {
    this.getPricesForProductsInputPort = getPricesForProductsInputPort;
    this.setProductPriceInputPort = setProductPriceInputPort;
  }

  /**
   * Price information DTO for cross-context communication.
   *
   * @param productId the product ID
   * @param currentPrice the current price
   * @param effectiveFrom when the price became effective
   */
  public record PriceInfo(ProductId productId, Money currentPrice, Instant effectiveFrom) {}

  /**
   * Retrieves prices for multiple products.
   *
   * @param productIds the collection of product IDs to get prices for
   * @return map of product IDs to their price info
   */
  public Map<ProductId, PriceInfo> getPrices(Collection<ProductId> productIds) {
    if (productIds.isEmpty()) {
      return Collections.emptyMap();
    }

    GetPricesForProductsResult result =
        getPricesForProductsInputPort.execute(new GetPricesForProductsQuery(productIds));

    return result.prices().entrySet().stream()
        .collect(
            Collectors.toMap(
                Map.Entry::getKey,
                entry ->
                    new PriceInfo(
                        entry.getValue().productId(),
                        entry.getValue().currentPrice(),
                        entry.getValue().effectiveFrom())));
  }

  /**
   * Retrieves price for a single product.
   *
   * @param productId the product ID
   * @return price info if found
   */
  public Optional<PriceInfo> getPrice(ProductId productId) {
    Map<ProductId, PriceInfo> prices = getPrices(Collections.singletonList(productId));
    return Optional.ofNullable(prices.get(productId));
  }

  /**
   * Sets the initial price for a product.
   *
   * <p>Delegates to the {@link SetProductPriceInputPort} use case. If no price exists for the
   * product, a new price record is created. If a price already exists, it is updated.
   *
   * @param productId the product ID
   * @param price the price to set (amount and currency)
   */
  public void setInitialPrice(ProductId productId, Money price) {
    setProductPriceInputPort.execute(
        new SetProductPriceCommand(
            productId.value(), price.amount(), price.currency().getCurrencyCode()));
  }
}
