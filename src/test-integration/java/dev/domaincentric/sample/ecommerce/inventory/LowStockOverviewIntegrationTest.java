package dev.domaincentric.sample.ecommerce.inventory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.domaincentric.sample.ecommerce.infrastructure.EcommerceSampleApplication;
import dev.domaincentric.sample.ecommerce.inventory.application.getlowstockproducts.GetLowStockProductsInputPort;
import dev.domaincentric.sample.ecommerce.inventory.application.getlowstockproducts.GetLowStockProductsQuery;
import dev.domaincentric.sample.ecommerce.inventory.application.getlowstockproducts.GetLowStockProductsResult;
import dev.domaincentric.sample.ecommerce.inventory.application.getlowstockproducts.GetLowStockProductsResult.LowStockProduct;
import dev.domaincentric.sample.ecommerce.inventory.application.setstocklevel.SetStockLevelCommand;
import dev.domaincentric.sample.ecommerce.inventory.application.setstocklevel.SetStockLevelInputPort;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.ProductId;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * The overview an operator asks for before a product runs out: which products hold less than the
 * quantity they name, each with the quantity still on hand. Driven through the wired application,
 * so the answer is the one a caller really gets.
 *
 * <p>No route drives this yet — the story names none, and nothing in the shop lets an operator ask
 * for the overview. Giving it a surface is a scoping question, so the overview is exercised at the
 * highest level that exists today: the input port in the running context.
 */
@SpringBootTest(classes = EcommerceSampleApplication.class)
@DisplayName("Low stock overview")
class LowStockOverviewIntegrationTest {

  @Autowired private SetStockLevelInputPort setStockLevel;

  @Autowired private GetLowStockProductsInputPort getLowStockProducts;

  @Test
  @DisplayName("Lists every product with less on hand than asked about")
  void listsEveryProductWithLessOnHandThanAskedAbout() {
    final ProductId nearlyGone = stockedWith(2);
    final ProductId running = stockedWith(3);

    final List<ProductId> listed = listedProducts(4);

    assertTrue(listed.contains(nearlyGone), "The product almost gone is running low");
    assertTrue(listed.contains(running), "The product below the quantity is running low");
  }

  @Test
  @DisplayName("Leaves out products with as much or more on hand than asked about")
  void leavesOutProductsWithAsMuchOrMoreOnHandThanAskedAbout() {
    final ProductId exactlyAtTheLimit = stockedWith(4);
    final ProductId wellStocked = stockedWith(9);

    final List<ProductId> listed = listedProducts(4);

    assertFalse(listed.contains(exactlyAtTheLimit), "A product at the quantity is not running low");
    assertFalse(listed.contains(wellStocked), "A product above the quantity is not running low");
  }

  @Test
  @DisplayName("Names how much of each listed product is left")
  void namesHowMuchOfEachListedProductIsLeft() {
    final ProductId nearlyGone = stockedWith(3);

    final Optional<LowStockProduct> listed =
        overviewBelow(4).products().stream()
            .filter(product -> product.productId().equals(nearlyGone))
            .findFirst();

    assertTrue(listed.isPresent(), "The product running low is part of the answer");
    assertEquals(3, listed.get().availableQuantity(), "The answer names what is left of it");
  }

  @Test
  @DisplayName("Answers with an empty list when nothing is running low")
  void answersWithAnEmptyListWhenNothingIsRunningLow() {
    stockedWith(6);

    assertTrue(overviewBelow(0).products().isEmpty(), "Nothing is below a quantity of none");
  }

  private GetLowStockProductsResult overviewBelow(final int quantity) {
    return getLowStockProducts.execute(new GetLowStockProductsQuery(quantity));
  }

  private List<ProductId> listedProducts(final int quantity) {
    return overviewBelow(quantity).products().stream().map(LowStockProduct::productId).toList();
  }

  private ProductId stockedWith(final int availableQuantity) {
    final ProductId productId = ProductId.generate();
    setStockLevel.execute(new SetStockLevelCommand(productId.value(), availableQuantity));
    return productId;
  }
}
