package dev.domaincentric.sample.ecommerce.account.adapter.outgoing.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import dev.domaincentric.sample.ecommerce.account.domain.gateway.PasswordHasher;
import dev.domaincentric.sample.ecommerce.account.domain.model.Account;
import dev.domaincentric.sample.ecommerce.account.domain.model.Email;
import dev.domaincentric.sample.ecommerce.account.domain.model.Owner;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.UserId;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * An email address belongs to one account, and so does a visitor identity. The store claims both
 * the way the {@code UNIQUE} columns of the schema do, so a check made before the save cannot be
 * overtaken between the two.
 */
class AccountUniquenessTest {

  private final InMemoryAccountRepository accounts = new InMemoryAccountRepository();

  @Test
  @DisplayName("A second account under a taken address is refused")
  void aSecondAccountUnderTheSameAddressIsRefused() {
    accounts.save(accountOf("ada@example.com", UserId.generateAnonymous()));

    assertThrows(
        IllegalStateException.class,
        () -> accounts.save(accountOf("ada@example.com", UserId.generateAnonymous())));
  }

  @Test
  @DisplayName("A second account for the same visitor is refused")
  void aSecondAccountForTheSameVisitorIsRefused() {
    final UserId visitor = UserId.generateAnonymous();
    accounts.save(accountOf("first@example.com", visitor));

    assertThrows(
        IllegalStateException.class, () -> accounts.save(accountOf("second@example.com", visitor)));
  }

  @Test
  @DisplayName("Saving the same account again keeps its address")
  void savingTheSameAccountAgainKeepsItsAddress() {
    final Account account =
        accounts.save(accountOf("grace@example.com", UserId.generateAnonymous()));

    accounts.save(account);

    assertEquals(
        account.id(), accounts.findByEmail(Email.of("grace@example.com")).orElseThrow().id());
  }

  private static Account accountOf(final String email, final UserId visitor) {
    return Account.register(
        Email.of(email),
        Owner.of("Ada", "Lovelace", LocalDate.of(1815, 12, 10)),
        "Secret123",
        visitor,
        new ReversiblePasswordHasher());
  }

  /** Reversible and deterministic; the hashing itself is not what is under test. */
  private static final class ReversiblePasswordHasher implements PasswordHasher {

    private static final String PREFIX = "hashed:";

    @Override
    public String hash(final String plaintext) {
      return PREFIX + plaintext;
    }

    @Override
    public boolean matches(final String plaintext, final String hash) {
      return hash.equals(PREFIX + plaintext);
    }
  }
}
