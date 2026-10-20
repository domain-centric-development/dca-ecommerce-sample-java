package dev.domaincentric.sample.ecommerce.checkout.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutValidationResult.ErrorType;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutValidationResult.ValidationError;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.ProductId;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("CheckoutValidationResult: the line a refusal for stock names")
class CheckoutValidationResultTest {

  @Test
  @DisplayName("the line short of stock is the first unavailable or short line, in line order")
  void theLineShortOfStockIsTheFirstUnavailableOrShortLine() {
    final ValidationError priceChanged =
        new ValidationError(
            ProductId.generate(), "Domain-Driven Design", "Price changed", ErrorType.PRICE_CHANGED);
    final ValidationError short1 =
        ValidationError.insufficientStock(ProductId.generate(), "Wooden Hexagon Desk Model", 5, 3);
    final ValidationError unavailable =
        ValidationError.productUnavailable(ProductId.generate(), "Brass Compass");

    final CheckoutValidationResult result =
        CheckoutValidationResult.withErrors(List.of(priceChanged, short1, unavailable));

    assertEquals(Optional.of(short1), result.shortOfStock());
  }

  @Test
  @DisplayName("a verdict that only found changed prices names no line short of stock")
  void aVerdictWithOnlyChangedPricesNamesNoLineShortOfStock() {
    final ValidationError priceChanged =
        new ValidationError(
            ProductId.generate(), "Domain-Driven Design", "Price changed", ErrorType.PRICE_CHANGED);

    final CheckoutValidationResult result = CheckoutValidationResult.withError(priceChanged);

    assertEquals(Optional.empty(), result.shortOfStock());
  }

  @Test
  @DisplayName("an error refuses a blank product name")
  void anErrorRefusesABlankProductName() {
    final ProductId productId = ProductId.generate();

    assertThrows(
        IllegalArgumentException.class,
        () -> ValidationError.insufficientStock(productId, " ", 5, 3));
  }
}
