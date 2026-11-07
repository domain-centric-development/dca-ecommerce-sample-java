package dev.domaincentric.sample.ecommerce.cart.adapter.incoming.api;

import dev.domaincentric.sample.ecommerce.cart.domain.model.CartId;
import dev.domaincentric.sample.ecommerce.cart.domain.model.CustomerId;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.ProductId;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

/**
 * The HTTP answers of the cart's refusals — the words this context's REST package uses for them.
 *
 * <p>{@link ShoppingCartResource} switches over each use case's result and takes the answer for a
 * refusal from here; {@link CartApiExceptionHandler} takes the answers for what is still thrown.
 * Note that the status does not follow where a refusal comes from: a position the cart does not
 * hold is a rule of the model and still answers {@code 404}, because what the caller has to do
 * about it is look for something that exists.
 *
 * <p>A detail is built from the refusal's values, never from a message.
 */
final class CartProblems {

  private CartProblems() {}

  /** No cart of this customer under that identity. */
  static ResponseEntity<ProblemDetail> cartNotFound(final CartId cartId) {
    return answer(
        problem(
            HttpStatus.NOT_FOUND, "Cart not found", "There is no cart " + cartId.value() + "."));
  }

  /**
   * The assortment carries no article for that product. The product is named in the body, not in
   * the address: the cart the address names is there, so this is {@code 422}, not {@code 404}.
   */
  static ResponseEntity<ProblemDetail> articleNotAvailable(final ProductId productId) {
    return answer(
        problem(
            HttpStatus.UNPROCESSABLE_ENTITY,
            "Article not available",
            "There is no article for product " + productId.value() + "."));
  }

  /** The cart does not hold the position the caller named. */
  static ResponseEntity<ProblemDetail> positionNotInCart(final String reference) {
    return answer(
        problem(
            HttpStatus.NOT_FOUND,
            "Position not in cart",
            "The cart holds no position for " + reference + "."));
  }

  /** Not enough of the article to promise the requested quantity. */
  static ResponseEntity<ProblemDetail> notEnoughStock(
      final ProductId productId, final int requested) {
    return answer(
        problem(
            HttpStatus.CONFLICT,
            "Not enough stock",
            "Requested "
                + requested
                + " of product "
                + productId.value()
                + ", more than is in stock."));
  }

  /** The customer already shops in another cart. */
  static ResponseEntity<ProblemDetail> activeCartAlreadyExists(final CustomerId customerId) {
    return answer(
        problem(
            HttpStatus.CONFLICT,
            "Active cart already exists",
            "Customer " + customerId.value() + " already has an active cart."));
  }

  /** The cart is no longer active and refuses the change. */
  static ResponseEntity<ProblemDetail> cartRefusesTheChange() {
    return answer(cartRefusesTheChangeProblem());
  }

  /**
   * A rule of the model refused the request — a cart that is no longer active, or any other rule a
   * use case does not return as a case of its own.
   */
  static ProblemDetail cartRefusesTheChangeProblem() {
    return problem(HttpStatus.CONFLICT, "Cart refuses the change");
  }

  /** A value the caller sent that the request refuses. */
  static ProblemDetail unacceptableValue() {
    return problem(HttpStatus.BAD_REQUEST, "Unacceptable value");
  }

  private static ResponseEntity<ProblemDetail> answer(final ProblemDetail problem) {
    return ResponseEntity.status(problem.getStatus()).body(problem);
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
