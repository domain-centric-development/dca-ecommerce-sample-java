package dev.domaincentric.sample.ecommerce.product.infrastructure;

import dev.domaincentric.sample.ecommerce.product.domain.model.ProductFactory;
import java.util.Random;
import java.util.random.RandomGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for Product context factories.
 *
 * <p>Factories are framework-independent but need to be instantiated as Spring beans so they can be
 * injected into application services.
 */
@Configuration
public class ProductDomainConfiguration {

  @Bean
  public ProductFactory productFactory() {
    return new ProductFactory();
  }

  /**
   * The random source the homepage's product selection is drawn with. {@link Random} is safe to
   * share between concurrent requests.
   */
  @Bean
  public RandomGenerator productSelectionRandom() {
    return new Random();
  }
}
