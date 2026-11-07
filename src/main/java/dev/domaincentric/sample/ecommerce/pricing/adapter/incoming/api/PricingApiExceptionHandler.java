package dev.domaincentric.sample.ecommerce.pricing.adapter.incoming.api;

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
 * Translates what is still <em>thrown</em> towards the pricing API into HTTP answers.
 *
 * <p>The price change returns its refusal as a case of its result, and {@link PriceResource}
 * switches over it. What reaches this handler is what no use case returns: a request the binding
 * refuses and a value that does not parse. The words are shared with the resource through {@link
 * PricingProblems}.
 *
 * <p>Scoped to this context's REST package: an adapter answers for its own module only (ADR-037).
 */
@RestControllerAdvice(
    basePackages = "dev.domaincentric.sample.ecommerce.pricing.adapter.incoming.api")
public class PricingApiExceptionHandler {

  private static final Logger LOG = LoggerFactory.getLogger(PricingApiExceptionHandler.class);

  /** The body names a value the request refuses; {@code errors} names each field and why. */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ProblemDetail handleRefusedField(final MethodArgumentNotValidException exception) {
    final Map<String, String> errors = new LinkedHashMap<>();
    for (final FieldError error : exception.getBindingResult().getFieldErrors()) {
      errors.putIfAbsent(error.getField(), error.getDefaultMessage());
    }
    final ProblemDetail problem = PricingProblems.unacceptableValue();
    problem.setProperty("errors", errors);
    return problem;
  }

  /**
   * A value the caller sent is not acceptable to a value object, or a product id that does not
   * parse in {@link PriceResource} — a malformed call either way. The one mapping that stays
   * imprecise: this is also the type a defect in this application raises, and nothing in the type
   * tells the two apart.
   */
  @ExceptionHandler(IllegalArgumentException.class)
  public ProblemDetail handleUnacceptableValue(final IllegalArgumentException exception) {
    LOG.debug("Rejected request value: {}", exception.getMessage());
    return PricingProblems.unacceptableValue();
  }
}
