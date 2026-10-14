package dev.domaincentric.sample.ecommerce.cart.adapter.outgoing.event;

import static org.assertj.core.api.Assertions.assertThat;

import dev.domaincentric.sample.ecommerce.cart.events.CartContentsChangedEvent;
import dev.domaincentric.sample.ecommerce.cart.events.CartContentsChangedEvent.ChangeType;
import dev.domaincentric.sample.ecommerce.infrastructure.EcommerceSampleApplication;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.modulith.events.core.EventSerializer;

/**
 * A {@link CartContentsChangedEvent} as the wired application's event publication registry stores
 * it: its change type is written as the value's code and read back to the same event.
 */
@SpringBootTest(classes = EcommerceSampleApplication.class)
@DisplayName("Cart contents changed event as stored")
class CartContentsChangedEventSerializationIntegrationTest {

  /** What each change type is published as; consumers read these, whatever the values are named. */
  private static final Map<ChangeType, String> PUBLISHED =
      Map.of(
          ChangeType.ITEM_ADDED, "ITEM_ADDED",
          ChangeType.ITEM_REMOVED, "ITEM_REMOVED",
          ChangeType.QUANTITY_CHANGED, "QUANTITY_CHANGED",
          ChangeType.CART_CLEARED, "CART_CLEARED");

  @Autowired private EventSerializer serializer;

  @ParameterizedTest
  @EnumSource(ChangeType.class)
  @DisplayName("Change type is stored as its code")
  void theChangeTypeIsStoredAsItsCodeAndReadBack(final ChangeType changeType) {
    final CartContentsChangedEvent event =
        CartContentsChangedEvent.now(UUID.randomUUID(), changeType);

    final Object stored = serializer.serialize(event);

    assertThat(stored.toString()).contains("\"changeType\":\"" + PUBLISHED.get(changeType) + "\"");
    assertThat(serializer.deserialize(stored, CartContentsChangedEvent.class)).isEqualTo(event);
  }
}
