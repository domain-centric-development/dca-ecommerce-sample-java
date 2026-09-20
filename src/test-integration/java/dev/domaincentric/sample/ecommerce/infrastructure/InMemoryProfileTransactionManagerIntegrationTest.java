package dev.domaincentric.sample.ecommerce.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * The {@code inmemory} profile runs on the same transaction manager as every other profile.
 *
 * <p>A placeholder manager that only pretends to open a transaction is not a cheaper variant of
 * one: the event publication registry writes on an auto-commit connection underneath it, so a
 * rolled-back use case still delivers its integration event.
 */
@SpringBootTest(
    classes = EcommerceSampleApplication.class,
    properties =
        "spring.datasource.url=jdbc:h2:mem:inmemory_tx_test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE")
@ActiveProfiles("inmemory")
class InMemoryProfileTransactionManagerIntegrationTest {

  @Autowired private PlatformTransactionManager transactionManager;

  @Test
  @DisplayName("The profile does not swap in a placeholder transaction manager")
  void theProfileKeepsTheRealTransactionManager() {
    assertThat(transactionManager.getClass().getName())
        .as("a manager that manages nothing cannot carry the event publication registry")
        .doesNotContain("InMemoryTransactionManager");
  }
}
