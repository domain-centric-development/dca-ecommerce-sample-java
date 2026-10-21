package dev.domaincentric.sample.ecommerce.product.adapter.incoming.api;

import dev.domaincentric.dca.buildingblocks.application.UseCaseException;
import dev.domaincentric.dca.buildingblocks.ddd.tactical.DomainException;
import dev.domaincentric.sample.ecommerce.product.application.createproduct.DuplicateSkuException;
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
 * Translates the catalog's failures into HTTP answers — the one place in this context that knows
 * the protocol.
 *
 * <p>The use cases raise types, not status codes: {@link DuplicateSkuException} says the catalog
 * already carries the stock keeping unit, and nothing in the application or the domain layer
 * decides what a caller is told about it. That decision lives here, so the same use case can serve
 * this REST exposure and the tool provider next to it with different answers.
 *
 * <p>The words are this adapter's own: a detail is built from the failure's values, never from its
 * message, which is for the log.
 *
 * <p>Scoped to this context's REST package: an adapter answers for its own module only (ADR-037).
 */
@RestControllerAdvice(
    basePackages = "dev.domaincentric.sample.ecommerce.product.adapter.incoming.api")
public class ProductApiExceptionHandler {

  private static final Logger LOG = LoggerFactory.getLogger(ProductApiExceptionHandler.class);

  /** The catalog already carries that stock keeping unit — a conflict with existing state. */
  @ExceptionHandler(DuplicateSkuException.class)
  public ProblemDetail handleDuplicateSku(final DuplicateSkuException exception) {
    return problem(
        HttpStatus.CONFLICT,
        "Stock keeping unit already in use",
        "A product with SKU " + exception.sku().value() + " already exists.");
  }

  /**
   * Any other failure the use case reports. The request was understood and refused for a stated
   * reason, which is what 422 means; a use-case failure that deserves its own status gets its own
   * handler above.
   */
  @ExceptionHandler(UseCaseException.class)
  public ProblemDetail handleUseCaseFailure(final UseCaseException exception) {
    return problem(HttpStatus.UNPROCESSABLE_ENTITY, "Request cannot be served");
  }

  /** A rule of the model refused the request. */
  @ExceptionHandler(DomainException.class)
  public ProblemDetail handleDomainRule(final DomainException exception) {
    return problem(HttpStatus.UNPROCESSABLE_ENTITY, "Business rule violated");
  }

  /**
   * The body names a value the request refuses — a blank name, a price of zero, a negative stock;
   * {@code errors} names each field as the request spells it, and why.
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
   * A value the caller sent is not acceptable to a value object — a malformed stock keeping unit,
   * an overlong name. This is the one mapping that stays imprecise: the same exception type is what
   * the runtime raises for a defect in this application, and nothing in the type tells the two
   * apart. Moving each such check into request validation is what removes the ambiguity, one field
   * at a time.
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
