package dev.domaincentric.sample.ecommerce.checkout.infrastructure;

import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutCartFactory;
import dev.domaincentric.sample.ecommerce.checkout.domain.service.CheckoutStepValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for Checkout context domain services.
 *
 * <p>Domain services and factories are framework-independent but need to be instantiated as Spring
 * beans so they can be injected into the application and web layers that use them.
 */
@Configuration
public class CheckoutDomainConfiguration {

  @Bean
  public CheckoutStepValidator checkoutStepValidator() {
    return new CheckoutStepValidator();
  }

  @Bean
  public CheckoutCartFactory checkoutCartFactory() {
    return new CheckoutCartFactory();
  }
}
