package dev.domaincentric.sample.ecommerce.pricing.adapter.incoming.api;

import dev.domaincentric.sample.ecommerce.pricing.application.changeprice.ChangePriceCommand;
import dev.domaincentric.sample.ecommerce.pricing.application.changeprice.ChangePriceInputPort;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST API Resource for a product's price: an operator changes it through the staff API.
 *
 * <p><b>Authorization:</b> changing a price is an operator action and demands the staff role,
 * stated as {@code @PreAuthorize} so the framework answers {@code 401} to a stranger and {@code
 * 403} to a customer without the role (ADR-036).
 *
 * <p>The product is addressed in the path; the answer to a change is {@code 204} without a body.
 */
@RestController
@RequestMapping("/api/products/{id}/price")
public class PriceResource {

  private final ChangePriceInputPort changePrice;

  public PriceResource(final ChangePriceInputPort changePrice) {
    this.changePrice = changePrice;
  }

  @PutMapping
  @PreAuthorize("hasRole('STAFF')")
  public ResponseEntity<Void> changePrice(
      @PathVariable final String id, @Valid @RequestBody final ChangePriceRequest request) {
    changePrice.execute(new ChangePriceCommand(parsedProductId(id), request.price()));
    return ResponseEntity.noContent().build();
  }

  /**
   * The product id the caller sent, parsed here: text that is no UUID is a malformed call, answered
   * {@code 400} by {@link PricingApiExceptionHandler} without reaching the use case — never a
   * product that does not exist.
   *
   * @throws IllegalArgumentException when the text does not parse
   */
  private static String parsedProductId(final String productId) {
    return UUID.fromString(productId).toString();
  }
}
