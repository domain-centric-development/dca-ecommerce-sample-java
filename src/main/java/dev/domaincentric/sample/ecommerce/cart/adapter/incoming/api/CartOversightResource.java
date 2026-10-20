package dev.domaincentric.sample.ecommerce.cart.adapter.incoming.api;

import dev.domaincentric.sample.ecommerce.cart.application.cartoversight.getallcarts.GetAllCartsInputPort;
import dev.domaincentric.sample.ecommerce.cart.application.cartoversight.getallcarts.GetAllCartsQuery;
import dev.domaincentric.sample.ecommerce.cart.application.cartoversight.getallcarts.GetAllCartsResult;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST API Resource for the cart oversight — the staff's view of every cart in the shop.
 *
 * <p>This is a primary adapter (incoming) in Hexagonal Architecture; it depends on the input port,
 * not on the use case class.
 *
 * <p><b>Authorization:</b> the only cart route that leaves the caller's own data. Crossing that
 * boundary demands the staff role; a stranger is challenged with {@code 401}, a customer without
 * the role forbidden with {@code 403} (ADR-036).
 *
 * <p><b>Bearer only:</b> {@code /api/**} is authenticated by an {@code Authorization: Bearer}
 * header and never by a browser cookie, which is what makes its CSRF exemption sound (ADR-035).
 */
@RestController
@RequestMapping("/api/carts")
public class CartOversightResource {

  private final GetAllCartsInputPort getAllCarts;
  private final ShoppingCartDtoConverter converter;

  public CartOversightResource(
      final GetAllCartsInputPort getAllCarts, final ShoppingCartDtoConverter converter) {
    this.getAllCarts = getAllCarts;
    this.converter = converter;
  }

  /** Every cart in the shop. */
  @GetMapping
  @PreAuthorize("hasRole('STAFF')")
  public ResponseEntity<ShoppingCartListDto> getAllCarts() {
    final GetAllCartsResult output = getAllCarts.execute(new GetAllCartsQuery());

    return ResponseEntity.ok(converter.toListDto(output));
  }
}
