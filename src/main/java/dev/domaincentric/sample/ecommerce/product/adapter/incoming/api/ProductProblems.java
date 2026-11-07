package dev.domaincentric.sample.ecommerce.product.adapter.incoming.api;

import dev.domaincentric.sample.ecommerce.product.domain.model.SKU;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

/**
 * The HTTP answers of the catalog's refusals — the words this context's REST package uses for them.
 *
 * <p>{@link ProductResource} switches over each use case's result and takes the answer for a
 * refusal from here; {@link ProductApiExceptionHandler} takes the answers for what is still thrown.
 * Nothing in the application or the domain layer decides what a caller is told about a refusal, so
 * the same use case can serve this REST exposure and the tool provider next to it with different
 * answers.
 *
 * <p>A detail is built from the refusal's values, never from a message.
 */
final class ProductProblems {

  private ProductProblems() {}

  /** The catalog already carries that stock keeping unit — a conflict with existing state. */
  static ResponseEntity<ProblemDetail> duplicateSku(final SKU sku) {
    final ProblemDetail problem =
        problem(
            HttpStatus.CONFLICT,
            "Stock keeping unit already in use",
            "A product with SKU " + sku.value() + " already exists.");
    return ResponseEntity.status(problem.getStatus()).body(problem);
  }

  /** A rule of the model refused the request. */
  static ProblemDetail businessRuleViolated() {
    return problem(HttpStatus.UNPROCESSABLE_ENTITY, "Business rule violated");
  }

  /** A value the caller sent that the request refuses. */
  static ProblemDetail unacceptableValue() {
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
