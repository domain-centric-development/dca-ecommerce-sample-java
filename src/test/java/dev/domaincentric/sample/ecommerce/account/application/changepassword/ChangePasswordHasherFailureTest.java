package dev.domaincentric.sample.ecommerce.account.application.changepassword;

import static dev.domaincentric.sample.ecommerce.account.application.changepassword.ChangePasswordTestFixtures.CURRENT_PASSWORD;
import static dev.domaincentric.sample.ecommerce.account.application.changepassword.ChangePasswordTestFixtures.USER_ID;
import static dev.domaincentric.sample.ecommerce.account.application.changepassword.ChangePasswordTestFixtures.accountWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import dev.domaincentric.sample.ecommerce.account.application.changepassword.ChangePasswordTestFixtures.TestAccountRepository;
import dev.domaincentric.sample.ecommerce.account.application.changepassword.ChangePasswordTestFixtures.TestDomainEventPublisher;
import dev.domaincentric.sample.ecommerce.account.application.changepassword.ChangePasswordTestFixtures.TestPasswordHasher;
import dev.domaincentric.sample.ecommerce.account.domain.gateway.PasswordHasher;
import dev.domaincentric.sample.ecommerce.account.domain.model.Account;
import dev.domaincentric.sample.ecommerce.account.domain.model.AccountStatus;
import dev.domaincentric.sample.ecommerce.account.domain.model.HashedPassword;
import dev.domaincentric.sample.ecommerce.account.domain.model.PasswordRule;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Pins which failures may become {@code NEW_PASSWORD_REJECTED}.
 *
 * <p>Only the domain's password-strength rules may. A hasher failure is a fault and must propagate:
 * {@code ChangePasswordPageController} renders the rejection message to the user verbatim, so
 * catching hasher failures would present an adapter fault — or BCrypt's 72-byte input limit — as if
 * it were a password rule.
 *
 * <p>These tests fail if the {@code try} in {@link ChangePasswordUseCase} is widened back around
 * {@code account.changePassword(...)}.
 */
@DisplayName("ChangePasswordUseCase — hasher failures are faults, not rejections")
class ChangePasswordHasherFailureTest {

  /** Strong enough for every strength rule, so only the hasher can reject it. */
  private static final String STRONG_NEW_PASSWORD = "Str0ngNewPassword";

  @Test
  @DisplayName("a hasher that rejects the plaintext propagates instead of rejecting the password")
  void hasherRejectionPropagates() {
    final PasswordHasher failing =
        hasherThatFailsOnHash("password cannot be more than 72 bytes", new TestPasswordHasher());

    final IllegalArgumentException thrown =
        assertThrows(
            IllegalArgumentException.class,
            () -> execute(failing, STRONG_NEW_PASSWORD),
            "a hasher failure must surface as a fault, not as NEW_PASSWORD_REJECTED");
    assertEquals("password cannot be more than 72 bytes", thrown.getMessage());
  }

  @Test
  @DisplayName("a hasher returning a blank hash propagates instead of rejecting the password")
  void blankHashPropagates() {
    final PasswordHasher blankHashing =
        new PasswordHasher() {
          private final TestPasswordHasher delegate = new TestPasswordHasher();

          @Override
          public String hash(final String plaintext) {
            return "   ";
          }

          @Override
          public boolean matches(final String plaintext, final String hash) {
            return delegate.matches(plaintext, hash);
          }
        };

    assertThrows(
        IllegalArgumentException.class,
        () -> execute(blankHashing, STRONG_NEW_PASSWORD),
        "a blank hash is an adapter bug and must surface as a fault");
  }

  @Test
  @DisplayName("a strength violation still yields NEW_PASSWORD_REJECTED")
  void strengthViolationStillRejects() {
    final ChangePasswordResult result = execute(new TestPasswordHasher(), "short");

    assertEquals(ChangePasswordResult.Outcome.NEW_PASSWORD_REJECTED, result.outcome());
  }

  @Test
  @DisplayName("an over-long password is rejected by the policy, never by the hasher")
  void overLongPasswordIsRejectedByThePolicy() {
    final String overLimit = "Aa1" + "x".repeat(HashedPassword.MAX_BYTE_LENGTH - 2);

    final ChangePasswordResult result = execute(new TestPasswordHasher(), overLimit);

    assertEquals(
        ChangePasswordResult.Outcome.NEW_PASSWORD_REJECTED,
        result.outcome(),
        "the length rule lives in the domain, so this never reaches the hasher");
    assertEquals(Optional.of(PasswordRule.TOO_LONG), result.brokenRule());
  }

  private static ChangePasswordResult execute(
      final PasswordHasher hasher, final String newPassword) {
    final TestPasswordHasher storing = new TestPasswordHasher();
    final Account account = accountWith(AccountStatus.ACTIVE, CURRENT_PASSWORD, storing);
    final List<String> interactions = new ArrayList<>();
    final TestAccountRepository repository = new TestAccountRepository(interactions);
    repository.save(account);
    interactions.clear();

    final PasswordHasher matchingButFailing = matchesWith(storing, hasher);
    return new ChangePasswordUseCase(
            repository, matchingButFailing, new TestDomainEventPublisher(interactions))
        .execute(new ChangePasswordCommand(USER_ID, CURRENT_PASSWORD, newPassword));
  }

  /** Verifies the current password like {@code storing} did, but hashes via {@code hashing}. */
  private static PasswordHasher matchesWith(
      final TestPasswordHasher storing, final PasswordHasher hashing) {
    return new PasswordHasher() {
      @Override
      public String hash(final String plaintext) {
        return hashing.hash(plaintext);
      }

      @Override
      public boolean matches(final String plaintext, final String hash) {
        return storing.matches(plaintext, hash);
      }
    };
  }

  private static PasswordHasher hasherThatFailsOnHash(
      final String message, final TestPasswordHasher delegate) {
    return new PasswordHasher() {
      @Override
      public String hash(final String plaintext) {
        throw new IllegalArgumentException(message);
      }

      @Override
      public boolean matches(final String plaintext, final String hash) {
        return delegate.matches(plaintext, hash);
      }
    };
  }
}
