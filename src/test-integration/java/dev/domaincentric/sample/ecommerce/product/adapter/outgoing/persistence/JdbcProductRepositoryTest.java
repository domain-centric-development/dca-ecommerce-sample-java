package dev.domaincentric.sample.ecommerce.product.adapter.outgoing.persistence;

import dev.domaincentric.sample.ecommerce.infrastructure.database.PostgresTestDatabase;
import dev.domaincentric.sample.ecommerce.product.application.shared.ProductRepository;
import javax.sql.DataSource;
import org.junit.jupiter.api.DisplayName;
import org.springframework.jdbc.core.JdbcTemplate;

/** Runs the {@link ProductRepositoryContractTest} against the JDBC adapter on PostgreSQL. */
@DisplayName("JdbcProductRepository")
class JdbcProductRepositoryTest extends ProductRepositoryContractTest {

  private static final DataSource DATA_SOURCE =
      PostgresTestDatabase.migrated("product-repository-contract");

  @Override
  ProductRepository createRepository() {
    new JdbcTemplate(DATA_SOURCE).execute("TRUNCATE products");
    return new JdbcProductRepository(DATA_SOURCE);
  }
}
