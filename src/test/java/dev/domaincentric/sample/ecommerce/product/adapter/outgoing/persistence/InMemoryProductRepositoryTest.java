package dev.domaincentric.sample.ecommerce.product.adapter.outgoing.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.domaincentric.sample.ecommerce.product.domain.model.Category;
import dev.domaincentric.sample.ecommerce.product.domain.model.Product;
import dev.domaincentric.sample.ecommerce.product.domain.model.ProductFactory;
import dev.domaincentric.sample.ecommerce.product.domain.model.ProductName;
import dev.domaincentric.sample.ecommerce.product.domain.model.SKU;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Money;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Price;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("InMemoryProductRepository")
class InMemoryProductRepositoryTest {

  private final ProductFactory factory = new ProductFactory();
  private final InMemoryProductRepository repository = new InMemoryProductRepository();

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

  private Product product(String sku, String name, String category) {
    return factory.createBasicProduct(
        SKU.of(sku), ProductName.of(name), Category.of(category), Price.of(Money.euro(1)), 1);
  }
}
