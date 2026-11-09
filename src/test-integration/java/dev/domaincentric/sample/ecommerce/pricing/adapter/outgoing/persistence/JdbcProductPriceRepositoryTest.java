package dev.domaincentric.sample.ecommerce.pricing.adapter.outgoing.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import dev.domaincentric.sample.ecommerce.infrastructure.database.PostgresTestDatabase;
import dev.domaincentric.sample.ecommerce.pricing.domain.model.ProductPrice;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Money;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Price;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.ProductId;
import java.util.List;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

@DisplayName("JdbcProductPriceRepository")
class JdbcProductPriceRepositoryTest {

  private static final DataSource DATA_SOURCE =
      PostgresTestDatabase.migrated("product-price-repository");

  private final JdbcProductPriceRepository prices = new JdbcProductPriceRepository(DATA_SOURCE);

  @BeforeEach
  void emptyTable() {
    new JdbcTemplate(DATA_SOURCE).execute("TRUNCATE product_prices");
  }

  @Test
  @DisplayName("A saved price reads back as it was saved, without events")
  void aSavedPriceReadsBack() {
    final ProductPrice saved = ProductPrice.create(ProductId.generate(), Price.of(Money.euro(12)));
    prices.save(saved);

    final ProductPrice loaded = prices.findByProductId(saved.productId()).orElseThrow();

    assertThat(loaded.id()).isEqualTo(saved.id());
    assertThat(loaded.currentPrice()).isEqualTo(saved.currentPrice());
    assertThat(loaded.effectiveFrom()).isEqualTo(saved.effectiveFrom());
    assertThat(loaded.domainEvents()).isEmpty();
  }

  @Test
  @DisplayName("A change survives only through save")
  void aChangeSurvivesOnlyThroughSave() {
    final ProductPrice saved = ProductPrice.create(ProductId.generate(), Price.of(Money.euro(12)));
    prices.save(saved);

    prices.findById(saved.id()).orElseThrow().updatePrice(Price.of(Money.euro(15)));
    assertThat(prices.findById(saved.id()).orElseThrow().currentPrice())
        .isEqualTo(Price.of(Money.euro(12)));

    final ProductPrice changed = prices.findById(saved.id()).orElseThrow();
    changed.updatePrice(Price.of(Money.euro(15)));
    prices.save(changed);
    assertThat(prices.findById(saved.id()).orElseThrow().currentPrice())
        .isEqualTo(Price.of(Money.euro(15)));
  }

  @Test
  @DisplayName("Prices of several products are found together, unknown ones skipped")
  void pricesOfSeveralProducts() {
    final ProductPrice first = prices.save(ProductPrice.create(ProductId.generate(), euro(1)));
    final ProductPrice second = prices.save(ProductPrice.create(ProductId.generate(), euro(2)));

    final List<ProductPrice> found =
        prices.findByProductIds(
            List.of(first.productId(), second.productId(), ProductId.generate()));

    assertThat(found)
        .extracting(ProductPrice::id)
        .containsExactlyInAnyOrder(first.id(), second.id());
    assertThat(prices.findByProductIds(List.of())).isEmpty();
  }

  private static Price euro(final int amount) {
    return Price.of(Money.euro(amount));
  }
}
