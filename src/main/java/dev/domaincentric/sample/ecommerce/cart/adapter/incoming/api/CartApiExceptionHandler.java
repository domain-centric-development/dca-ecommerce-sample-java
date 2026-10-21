package dev.domaincentric.sample.ecommerce.cart.adapter.incoming.api;

import dev.domaincentric.dca.buildingblocks.application.UseCaseException;
import dev.domaincentric.dca.buildingblocks.ddd.tactical.DomainException;
import dev.domaincentric.sample.ecommerce.cart.application.shared.ActiveCartAlreadyExistsException;
import dev.domaincentric.sample.ecommerce.cart.application.shared.CartNotFoundException;
import dev.domaincentric.sample.ecommerce.cart.application.shopping.additemtocart.ArticleNotAvailableException;
import dev.domaincentric.sample.ecommerce.cart.application.shopping.additemtocart.InsufficientArticleStockException;
import dev.domaincentric.sample.ecommerce.cart.domain.model.CartItemNotFoundException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Translates the cart's failures into HTTP answers — the one place in this context that knows the
 * protocol.
 *
 * <p>Each outcome has a type, so the mapping is a table rather than a chain of message tests. Note
 * that the status does not follow the base type: a position the cart does not hold is a rule of the
 * model and still answers {@code 404}, because what the caller has to do about it is look for
 * something that exists. Deciding that is this adapter's job and nothing further in.
 *
 * <p>The words are this adapter's own: a detail is built from the failure's values, never from its
 * message, which is for the log.
 *
 * <p>Scoped to this context's REST package: an adapter answers for its own module only (ADR-037).
 */
@RestControllerAdvice(basePackages = "dev.domaincentric.sample.ecommerce.cart.adapter.incoming.api")
public class CartApiExceptionHandler {

  private static final Logger LOG = LoggerFactory.getLogger(CartApiExceptionHandler.class);

  /** No cart of this customer under that identity. */
  @ExceptionHandler(CartNotFoundException.class)
  public ProblemDetail handleCartNotFound(final CartNotFoundException exception) {
    return problem(
        HttpStatus.NOT_FOUND,
        "Cart not found",
        "There is no cart " + exception.cartId().value() + ".");
  }

  /**
   * The assortment carries no article for that product. The product is named in the body, not in
   * the address: the cart the address names is there, so this is {@code 422}, not {@code 404}.
   */
  @ExceptionHandler(ArticleNotAvailableException.class)
  public ProblemDetail handleArticleNotAvailable(final ArticleNotAvailableException exception) {
    return problem(
        HttpStatus.UNPROCESSABLE_ENTITY,
        "Article not available",
        "There is no article for product " + exception.productId().value() + ".");
  }

  /** The cart does not hold the position the caller named. */
  @ExceptionHandler(CartItemNotFoundException.class)
  public ProblemDetail handleCartItemNotFound(final CartItemNotFoundException exception) {
    return problem(
        HttpStatus.NOT_FOUND,
        "Position not in cart",
        "The cart holds no position for " + exception.reference() + ".");
  }

  /** Not enough of the article to promise the requested quantity. */
  @ExceptionHandler(InsufficientArticleStockException.class)
  public ProblemDetail handleInsufficientStock(final InsufficientArticleStockException exception) {
    return problem(
        HttpStatus.CONFLICT,
        "Not enough stock",
        "Requested "
            + exception.requested()
            + " of product "
            + exception.productId().value()
            + ", more than is in stock.");
  }

  /** The customer already shops in another cart. */
  @ExceptionHandler(ActiveCartAlreadyExistsException.class)
  public ProblemDetail handleActiveCartExists(final ActiveCartAlreadyExistsException exception) {
    return problem(
        HttpStatus.CONFLICT,
        "Active cart already exists",
        "Customer " + exception.customerId().value() + " already has an active cart.");
  }

  /** Any other failure the use case reports. */
  @ExceptionHandler(UseCaseException.class)
  public ProblemDetail handleUseCaseFailure(final UseCaseException exception) {
    return problem(HttpStatus.UNPROCESSABLE_ENTITY, "Request cannot be served");
  }

  /**
   * A rule of the model refused the request — a cart that is no longer active, a completion that
   * already happened.
   */
  @ExceptionHandler(DomainException.class)
  public ProblemDetail handleDomainRule(final DomainException exception) {
    return problem(HttpStatus.CONFLICT, "Cart refuses the change");
  }

  /**
   * The body names a value the request refuses — a quantity below one; {@code errors} names each
   * field as the request spells it, and why.
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ProblemDetail handleRefusedField(final MethodArgumentNotValidException exception) {
    final Map<String, String> errors = new LinkedHashMap<>();
    for (final FieldError error : exception.getBindingResult().getFieldErrors()) {
      errors.putIfAbsent(error.getField(), error.getDefaultMessage());
    }
    final ProblemDetail problem = unacceptableValue();
    problem.setProperty("errors", errors);
    return problem;
  }

  /**
   * A value the caller sent is not acceptable to a value object, or a product id that does not
   * parse in {@link ShoppingCartResource} — a malformed call either way. The one mapping that stays
   * imprecise: this is also the type a defect in this application raises, and nothing in the type
   * tells the two apart. Moving each such check into request validation removes the ambiguity, one
   * field at a time.
   */
  @ExceptionHandler(IllegalArgumentException.class)
  public ProblemDetail handleUnacceptableValue(final IllegalArgumentException exception) {
    LOG.debug("Rejected request value: {}", exception.getMessage());
    return unacceptableValue();
  }

  private static ProblemDetail unacceptableValue() {
    return problem(HttpStatus.BAD_REQUEST, "Unacceptable value");
  }

  private static ProblemDetail problem(
      final HttpStatus status, final String title, final String detail) {
    final ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
    problem.setTitle(title);
    return problem;
  }

  /** A refusal the title names completely: no detail beyond it. */
  private static ProblemDetail problem(final HttpStatus status, final String title) {
    final ProblemDetail problem = ProblemDetail.forStatus(status);
    problem.setTitle(title);
    return problem;
  }
}
