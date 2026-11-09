package dev.domaincentric.sample.ecommerce.infrastructure;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main Spring Boot Application for AI Architecture Sample.
 *
 * <p>This application demonstrates Domain-Driven Design (DDD), Hexagonal Architecture, and Onion
 * Architecture patterns with an e-commerce domain (Product Catalog and Shopping Cart).
 *
 * <p><b>Package Location:</b> Located in {@code infrastructure} because the Spring Boot application
 * class is framework-specific infrastructure code, not part of any bounded context.
 *
 * <p><b>Component Scanning:</b> Explicitly configured to scan the entire {@code
 * dev.domaincentric.sample.ecommerce} package tree to discover all bounded contexts (product, cart,
 * portal) and shared kernel components.
 */
@SpringBootApplication(scanBasePackages = "dev.domaincentric.sample.ecommerce")
public class EcommerceSampleApplication {

  public static void main(final String[] args) {
    SpringApplication.run(EcommerceSampleApplication.class, args);
  }
}
