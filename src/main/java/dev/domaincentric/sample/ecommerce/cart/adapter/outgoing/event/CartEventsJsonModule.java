package dev.domaincentric.sample.ecommerce.cart.adapter.outgoing.event;

import com.fasterxml.jackson.annotation.JsonValue;
import dev.domaincentric.sample.ecommerce.cart.events.CartContentsChangedEvent;
import org.springframework.stereotype.Component;
import tools.jackson.databind.module.SimpleModule;

/**
 * How the cart's integration events are written as JSON when the event publication registry stores
 * them: a {@link CartContentsChangedEvent.ChangeType} is written as its {@code code()}, never as
 * the name of the enum value, and read back from it. The published language stays free of framework
 * annotations; this module carries them.
 */
@Component
public class CartEventsJsonModule extends SimpleModule {

  public CartEventsJsonModule() {
    super(CartEventsJsonModule.class.getSimpleName());
    setMixInAnnotation(CartContentsChangedEvent.ChangeType.class, ChangeTypeAsCode.class);
  }

  /** Puts {@link JsonValue} on {@code ChangeType.code()}. */
  private abstract static class ChangeTypeAsCode {

    @JsonValue
    abstract String code();
  }
}
