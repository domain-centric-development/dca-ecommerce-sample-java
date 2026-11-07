package dev.domaincentric.sample.ecommerce.pricing.application.changeprice;

import dev.domaincentric.dca.buildingblocks.application.Refusal;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.ProductId;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * What changing a product's price came to: the price changed, or Pricing holds no price for the
 * product.
 */
public sealed interface ChangePriceResult
    permits ChangePriceResult.Changed, ChangePriceResult.ProductPriceNotFound {

  /**
   * The price changed.
   *
   * @param productId the product whose price changed
   * @param priceAmount the price's amount now
   * @param priceCurrency the price's currency code
   * @param effectiveFrom when the new price became effective
   */
  record Changed(
      String productId, BigDecimal priceAmount, String priceCurrency, Instant effectiveFrom)
      implements ChangePriceResult {}

  /**
   * Pricing holds no price for the addressed product — every product of the catalogue has one, so
   * there is no such product. Nothing was written.
   *
   * @param productId the product the caller named
   */
  record ProductPriceNotFound(ProductId productId) implements ChangePriceResult, Refusal {}
}
