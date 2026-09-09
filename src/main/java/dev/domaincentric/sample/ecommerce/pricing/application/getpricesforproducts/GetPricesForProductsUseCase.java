package dev.domaincentric.sample.ecommerce.pricing.application.getpricesforproducts;

import dev.domaincentric.sample.ecommerce.pricing.application.shared.ProductPriceRepository;
import dev.domaincentric.sample.ecommerce.pricing.domain.model.ProductPrice;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.ProductId;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case for retrieving prices for multiple products.
 *
 * <p>This is a query use case that retrieves product prices without modifying state.
 *
 * <p><b>Hexagonal Architecture:</b> This class implements the {@link GetPricesForProductsInputPort}
 * interface, which is a primary/driving port in the application layer.
 */
@Service
@Transactional(readOnly = true)
public class GetPricesForProductsUseCase implements GetPricesForProductsInputPort {

  private final ProductPriceRepository productPriceRepository;

  public GetPricesForProductsUseCase(final ProductPriceRepository productPriceRepository) {
    this.productPriceRepository = productPriceRepository;
  }

  @Override
  public GetPricesForProductsResult execute(final GetPricesForProductsQuery query) {
    final List<ProductPrice> prices = productPriceRepository.findByProductIds(query.productIds());

    final Map<ProductId, GetPricesForProductsResult.PriceData> priceMap =
        prices.stream()
            .collect(
                Collectors.toMap(
                    ProductPrice::productId,
                    price ->
                        new GetPricesForProductsResult.PriceData(
                            price.productId(),
                            price.currentPrice().value(),
                            price.effectiveFrom())));

    return new GetPricesForProductsResult(priceMap);
  }
}
