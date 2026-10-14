package dev.domaincentric.sample.ecommerce.account.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RoleTest {

  @Test
  @DisplayName("every role is found by its code")
  void everyRoleIsFoundByItsCode() {
    for (final Role role : Role.values()) {
      assertEquals(role, Role.fromCode(role.code()));
    }
  }

  @Test
  @DisplayName("the codes are the spelling the token and the store hold")
  void codesAreTheSpellingTheTokenAndTheStoreHold() {
    assertEquals("CUSTOMER", Role.CUSTOMER.code());
    assertEquals("STAFF", Role.STAFF.code());
  }

  @Test
  @DisplayName("an unknown code is refused")
  void unknownCodeIsRefused() {
    assertThrows(IllegalArgumentException.class, () -> Role.fromCode("ADMIN"));
  }
}
