package dev.domaincentric.sample.ecommerce.e2e.pages;

import com.microsoft.playwright.Locator;
import java.util.List;

/** The items of an order summary, as the checkout pages list them. */
final class OrderSummaryItems {

  private OrderSummaryItems() {}

  /**
   * Reads the items inside a container.
   *
   * @param container the element holding the order summary's items
   * @return each as "name xQuantity price", e.g. {@code "Domain-Driven Design x1 54.99 EUR"}
   */
  static List<String> of(final Locator container) {
    container.locator(".order-summary__item").first().waitFor();
    return container.locator(".order-summary__item").all().stream()
        .map(
            item ->
                item.locator(".order-summary__item-name").textContent().trim()
                    + " "
                    + item.locator(".order-summary__item-qty").textContent().trim()
                    + " "
                    + item.locator(".order-summary__item-price").textContent().trim())
        .toList();
  }
}
