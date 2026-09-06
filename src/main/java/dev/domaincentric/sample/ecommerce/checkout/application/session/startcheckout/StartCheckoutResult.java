package dev.domaincentric.sample.ecommerce.checkout.application.startcheckout;

import java.util.List;

/**
 * Output model for checkout session creation.
 *
 * @param sessionId the generated checkout session ID
 * @param cartId the source cart ID
 * @param customerId the customer ID
 * @param currentStep the current checkout step
 * @param status the session status
 * @param lineItems the line items in the checkout
 * @param subtotal the subtotal amount as string
 */
public record StartCheckoutResult(
    String sessionId,
    String cartId,
    String customerId,
    String currentStep,
    String status,
    List<LineItemData> lineItems,
    String subtotal) {

  /**
   * Line item details in the result.
   *
   * @param lineItemId the line item ID
   * @param productId the product ID
   * @param productName the product name
   * @param unitPrice the unit price as string
   * @param quantity the quantity
   * @param lineTotal the line total as string
   */
  public record LineItemData(
      String lineItemId,
      String productId,
      String productName,
      String unitPrice,
      int quantity,
      String lineTotal) {}
}
