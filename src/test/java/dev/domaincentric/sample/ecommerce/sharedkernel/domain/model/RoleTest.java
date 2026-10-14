package dev.domaincentric.sample.ecommerce.sharedkernel.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RoleTest {

  @Test
  @DisplayName("every role is found by its code")
  void everyRoleIsFoundByItsCode() {
    for (final Role role : Role.all()) {
      assertSame(role, Role.fromCode(role.code()));
    }
  }

  @Test
  @DisplayName("the roles are customer and staff, nothing else")
  void theRolesAreCustomerAndStaff() {
    assertEquals(Set.of(Role.CUSTOMER, Role.STAFF), Role.all());
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

  @Test
  @DisplayName("a role built by hand with an unknown code is refused")
  void roleBuiltByHandWithUnknownCodeIsRefused() {
    assertThrows(IllegalArgumentException.class, () -> new Role("ADMIN"));
  }

  @Test
  @DisplayName("a role built by hand from a known code equals the constant")
  void roleBuiltByHandEqualsTheConstant() {
    assertEquals(Role.STAFF, new Role("STAFF"));
  }
}
