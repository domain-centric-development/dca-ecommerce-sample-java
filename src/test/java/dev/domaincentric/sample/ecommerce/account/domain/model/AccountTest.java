package dev.domaincentric.sample.ecommerce.account.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.domaincentric.dca.buildingblocks.ddd.tactical.DomainEvent;
import dev.domaincentric.sample.ecommerce.account.domain.event.AccountEmailChanged;
import dev.domaincentric.sample.ecommerce.account.domain.event.AccountOwnerDateOfBirthChanged;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.UserId;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the profile-changing behaviour of the {@link Account} aggregate.
 *
 * <p>Pins that changing the email or the owner's date of birth raises its event only on an actual
 * change and is refused on a closed account, and that no operation can change the owner's name.
 */
@DisplayName("Account profile changes")
class AccountTest {

  private static final String EMAIL = "jane.doe@example.com";
  private static final String NEW_EMAIL = "jane.new@example.com";
  private static final LocalDate DATE_OF_BIRTH = LocalDate.of(1990, 5, 17);
  private static final LocalDate CORRECTED_DATE_OF_BIRTH = LocalDate.of(1990, 5, 18);
  private static final Owner OWNER = Owner.of("Jane", "Doe", DATE_OF_BIRTH);
  private static final Instant CREATED_AT = Instant.parse("2026-01-01T00:00:00Z");

  /**
   * Any member of the aggregate that mentions a name at all — the aggregate holds the owner as a
   * whole and never touches the name parts, so not even a reader is expected here.
   */
  private static final Pattern NAME_MEMBER = Pattern.compile(".*name.*");

  private static Account accountWith(final AccountStatus status) {
    return Account.reconstitute(
        AccountId.of("account-1"),
        Email.of(EMAIL),
        OWNER,
        UserId.of("user-4711"),
        HashedPassword.of("hashed:OldPassw0rd"),
        status,
        Set.of("CUSTOMER"),
        CREATED_AT,
        Instant.parse("2026-07-31T08:15:30Z"));
  }

  private static Account activeAccount() {
    return accountWith(AccountStatus.ACTIVE);
  }

  // ---------------------------------------------------------------- reconstitution

  @Test
  @DisplayName("reconstitution restores the stored creation timestamp")
  void reconstitutionRestoresCreationTimestamp() {
    assertEquals(CREATED_AT, activeAccount().createdAt());
  }

  @Test
  @DisplayName("reconstitution restores the stored owner")
  void reconstitutionRestoresOwner() {
    assertEquals(OWNER, activeAccount().owner());
  }

  // ---------------------------------------------------------------- email

  @Test
  @DisplayName("changing the email stores the new address")
  void changingEmailStoresNewAddress() {
    final Account account = activeAccount();

    account.changeEmail(Email.of(NEW_EMAIL));

    assertEquals(Email.of(NEW_EMAIL), account.email());
  }

  @Test
  @DisplayName("changing the email raises the email changed event with both addresses")
  void changingEmailRaisesEvent() {
    final Account account = activeAccount();

    account.changeEmail(Email.of(NEW_EMAIL));

    final List<DomainEvent> events = List.copyOf(account.domainEvents());
    assertEquals(1, events.size(), "an email change raises exactly one event");
    final AccountEmailChanged event =
        assertInstanceOf(AccountEmailChanged.class, events.getFirst());
    assertEquals(AccountId.of("account-1"), event.accountId());
    assertEquals(Email.of(EMAIL), event.previousEmail());
    assertEquals(Email.of(NEW_EMAIL), event.newEmail());
  }

  @Test
  @DisplayName("re-submitting the stored email raises no event")
  void unchangedEmailRaisesNoEvent() {
    final Account account = activeAccount();
    account.changeEmail(Email.of(NEW_EMAIL));
    assertEquals(
        Email.of(NEW_EMAIL), account.email(), "precondition: the first change must be stored");
    account.clearDomainEvents();

    account.changeEmail(Email.of(NEW_EMAIL.toUpperCase(Locale.ROOT)));

    assertTrue(
        account.domainEvents().isEmpty(),
        "the stored address submitted again is not a change, whatever its spelling");
    assertEquals(Email.of(NEW_EMAIL), account.email());
  }

  @Test
  @DisplayName("a closed account refuses an email change")
  void closedAccountRefusesEmailChange() {
    final Account account = accountWith(AccountStatus.CLOSED);

    assertThrows(AccountClosedException.class, () -> account.changeEmail(Email.of(NEW_EMAIL)));
    assertEquals(Email.of(EMAIL), account.email(), "the refused change must store nothing");
  }

  // ---------------------------------------------------------------- date of birth

  @Test
  @DisplayName("correcting the date of birth stores it and raises its event")
  void changingDateOfBirthStoresItAndRaisesEvent() {
    final Account account = activeAccount();

    account.changeOwnerDateOfBirth(CORRECTED_DATE_OF_BIRTH);

    assertEquals(CORRECTED_DATE_OF_BIRTH, account.owner().dateOfBirth());
    final List<DomainEvent> events = List.copyOf(account.domainEvents());
    assertEquals(1, events.size(), "a date of birth change raises exactly one event");
    final AccountOwnerDateOfBirthChanged event =
        assertInstanceOf(AccountOwnerDateOfBirthChanged.class, events.getFirst());
    assertEquals(DATE_OF_BIRTH, event.previousDateOfBirth(), "a correction names what it replaced");
    assertEquals(CORRECTED_DATE_OF_BIRTH, event.newDateOfBirth());
  }

  @Test
  @DisplayName("correcting the date of birth leaves both names untouched")
  void changingDateOfBirthKeepsNames() {
    final Account account = activeAccount();

    account.changeOwnerDateOfBirth(CORRECTED_DATE_OF_BIRTH);

    assertEquals(OWNER.firstName(), account.owner().firstName());
    assertEquals(OWNER.lastName(), account.owner().lastName());
  }

  @Test
  @DisplayName("re-submitting the stored date of birth raises no event")
  void unchangedDateOfBirthRaisesNoEvent() {
    final Account account = activeAccount();

    account.changeOwnerDateOfBirth(DATE_OF_BIRTH);

    assertTrue(account.domainEvents().isEmpty(), "an unchanged date of birth is not a change");
  }

  @Test
  @DisplayName("a future date of birth is refused")
  void futureDateOfBirthIsRefused() {
    final Account account = activeAccount();
    final LocalDate tomorrow = LocalDate.now().plusDays(1);

    assertThrows(IllegalArgumentException.class, () -> account.changeOwnerDateOfBirth(tomorrow));
    assertEquals(
        DATE_OF_BIRTH, account.owner().dateOfBirth(), "the refused change must store nothing");
  }

  @Test
  @DisplayName("a closed account refuses a date of birth change")
  void closedAccountRefusesDateOfBirthChange() {
    final Account account = accountWith(AccountStatus.CLOSED);

    assertThrows(
        AccountClosedException.class,
        () -> account.changeOwnerDateOfBirth(CORRECTED_DATE_OF_BIRTH));
    assertEquals(
        DATE_OF_BIRTH, account.owner().dateOfBirth(), "the refused change must store nothing");
  }

  // ---------------------------------------------------------------- the name never changes

  @Test
  @DisplayName("no operation on an existing account accepts an owner, so none can replace the name")
  void noOperationReplacesTheOwner() {
    for (final Method method : Account.class.getDeclaredMethods()) {
      if (Modifier.isStatic(method.getModifiers())) {
        // The factories are out of scope here: register legitimately introduces the name, and
        // reconstitute is a known gap this guard does not close (see ADR-028, "Open question").
        continue;
      }
      assertFalse(
          List.of(method.getParameterTypes()).contains(Owner.class),
          "an operation taking an Owner could swap the name, found: " + method.getName());
    }
  }

  @Test
  @DisplayName("the aggregate declares no member that mentions a name")
  void declaresNoNameMember() {
    for (final Method method : Account.class.getDeclaredMethods()) {
      assertFalse(
          NAME_MEMBER.matcher(method.getName().toLowerCase(Locale.ROOT)).matches(),
          "the name belongs to the Owner, not to the aggregate, found: " + method.getName());
    }
    for (final java.lang.reflect.Field field : Account.class.getDeclaredFields()) {
      assertFalse(
          NAME_MEMBER.matcher(field.getName().toLowerCase(Locale.ROOT)).matches(),
          "the name belongs to the Owner, not to the aggregate, found: " + field.getName());
    }
  }

  @Test
  @DisplayName("the owner is immutable, so a held reference cannot be used to rename")
  void ownerIsImmutable() {
    assertTrue(Owner.class.isRecord(), "Owner must be a record for its components to be final");
  }
}
