package dev.domaincentric.sample.ecommerce.product.adapter.outgoing.persistence;

import dev.domaincentric.sample.ecommerce.product.application.shared.ProductRepository;
import org.junit.jupiter.api.DisplayName;

@DisplayName("InMemoryProductRepository")
class InMemoryProductRepositoryTest extends ProductRepositoryContractTest {

  @Override
  ProductRepository createRepository() {
    return new InMemoryProductRepository();
  }
}
