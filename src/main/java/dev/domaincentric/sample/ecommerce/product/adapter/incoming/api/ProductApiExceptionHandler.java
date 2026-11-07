package dev.domaincentric.sample.ecommerce.product.adapter.incoming.api;

import dev.domaincentric.dca.buildingblocks.ddd.tactical.DomainException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Translates what is still <em>thrown</em> towards the catalog's REST package into HTTP answers.
 *
 * <p>The catalog's use cases return every refusal a caller answers as a case of their result, and
 * {@link ProductResource} switches over it. What reaches this handler is what no use case returns:
 * a request the binding refuses, a value that does not parse, and a rule of the model no use case
 * names as a case of its own. The words are shared with the resource through {@link
 * ProductProblems}.
 *
 * <p>Scoped to this context's REST package: an adapter answers for its own module only (ADR-037).
 */
@RestControllerAdvice(
    basePackages = "dev.domaincentric.sample.ecommerce.product.adapter.incoming.api")
public class ProductApiExceptionHandler {

  private static final Logger LOG = LoggerFactory.getLogger(ProductApiExceptionHandler.class);

  /** A rule of the model refused the request that no use case returns as a case. */
  @ExceptionHandler(DomainException.class)
  public ProblemDetail handleDomainRule(final DomainException exception) {
    return ProductProblems.businessRuleViolated();
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
    final ProblemDetail problem = ProductProblems.unacceptableValue();
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
    return ProductProblems.unacceptableValue();
  }
}
