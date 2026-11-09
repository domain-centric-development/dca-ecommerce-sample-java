package dev.domaincentric.sample.ecommerce.product.adapter.outgoing.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.domaincentric.sample.ecommerce.product.application.shared.ProductRepository;
import dev.domaincentric.sample.ecommerce.product.domain.model.Category;
import dev.domaincentric.sample.ecommerce.product.domain.model.Product;
import dev.domaincentric.sample.ecommerce.product.domain.model.ProductFactory;
import dev.domaincentric.sample.ecommerce.product.domain.model.ProductName;
import dev.domaincentric.sample.ecommerce.product.domain.model.SKU;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Money;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Price;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The behaviour every {@link ProductRepository} adapter owes its callers, run against each
 * implementation (ADR-031): listings in name order, and a SKU claimed the way a unique column
 * claims it, so a check made before the save cannot be overtaken between the two.
 */
abstract class ProductRepositoryContractTest {

  private final ProductFactory factory = new ProductFactory();
  private ProductRepository repository;

  /**
   * Provides the adapter under test, freshly emptied.
   *
   * @return the repository implementation
   */
  abstract ProductRepository createRepository();

  @BeforeEach
  void setUpRepository() {
    repository = createRepository();
  }

  @Test
  @DisplayName("lists products ordered by name, whatever the order they were saved in")
  void findAllIsOrderedByProductName() {
    repository.save(product("SKU-3", "Team Topologies", "Books"));
    repository.save(product("SKU-1", "Hexagon Sticker Sheet", "Stickers"));
    repository.save(product("SKU-2", "Clean Architecture", "Books"));
    repository.save(product("SKU-4", "\"Ports & Adapters\" T-Shirt", "Apparel"));

    List<String> names =
        repository.findAll().stream().map(product -> product.name().value()).toList();

    assertEquals(
        List.of(
            "\"Ports & Adapters\" T-Shirt",
            "Clean Architecture",
            "Hexagon Sticker Sheet",
            "Team Topologies"),
        names);
  }

  @Test
  @DisplayName("lists a category ordered by name as well")
  void findByCategoryIsOrderedByProductName() {
    repository.save(product("SKU-3", "Team Topologies", "Books"));
    repository.save(product("SKU-1", "Hexagon Sticker Sheet", "Stickers"));
    repository.save(product("SKU-2", "Clean Architecture", "Books"));

    List<String> names =
        repository.findByCategory(Category.of("Books")).stream()
            .map(product -> product.name().value())
            .toList();

    assertEquals(List.of("Clean Architecture", "Team Topologies"), names);
  }

  @Test
  @DisplayName("A second product under a taken SKU is refused")
  void aSecondProductUnderTheSameSkuIsRefused() {
    repository.save(product("SKU-TAKEN", "Thing", "Books"));

    assertThrows(
        IllegalStateException.class,
        () -> repository.save(product("SKU-TAKEN", "Other thing", "Books")));
  }

  @Test
  @DisplayName("Saving the same product again keeps its SKU")
  void savingTheSameProductAgainKeepsItsSku() {
    final Product product = repository.save(product("SKU-OWN", "Thing", "Books"));

    repository.save(product);

    assertEquals(product.id(), repository.findBySku(SKU.of("SKU-OWN")).orElseThrow().id());
  }

  @Test
  @DisplayName("A deleted product is gone, its SKU free again")
  void aDeletedProductIsGone() {
    final Product product = repository.save(product("SKU-GONE", "Thing", "Books"));

    repository.deleteById(product.id());

    assertTrue(repository.findById(product.id()).isEmpty());
    assertFalse(repository.existsBySku(SKU.of("SKU-GONE")));
  }

  private Product product(final String sku, final String name, final String category) {
    return factory.createBasicProduct(
        SKU.of(sku), ProductName.of(name), Category.of(category), Price.of(Money.euro(1)), 1);
  }
}
