package dev.domaincentric.sample.ecommerce.pricing.adapter.incoming.api;

import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.ProductId;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

/**
 * The HTTP answers of the pricing API's refusals — the words this context's REST package uses for
 * them.
 *
 * <p>{@link PriceResource} switches over the use case's result and takes the answer for a refusal
 * from here; {@link PricingApiExceptionHandler} takes the answers for what is still thrown. A
 * detail is built from the refusal's values, never from a message.
 */
final class PricingProblems {

  private PricingProblems() {}

  /** Pricing holds no price for the product — there is no such product. */
  static ResponseEntity<ProblemDetail> productNotFound(final ProductId productId) {
    final ProblemDetail problem =
        ProblemDetail.forStatusAndDetail(
            HttpStatus.NOT_FOUND, "There is no product " + productId.value() + ".");
    problem.setTitle("Product not found");
    return ResponseEntity.status(problem.getStatus()).body(problem);
  }

  /** A value the caller sent that the request refuses. */
  static ProblemDetail unacceptableValue() {
    final ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
    problem.setTitle("Unacceptable value");
    return problem;
  }
}
