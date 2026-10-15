package dev.domaincentric.sample.ecommerce.pricing.adapter.incoming.api;

import dev.domaincentric.sample.ecommerce.pricing.application.changeprice.ProductPriceNotFoundException;
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
 * Translates the pricing API's refusals into HTTP answers — the one place in this context that
 * knows the protocol.
 *
 * <p>The words are this adapter's own: a detail is built from the failure's values, never from its
 * message, which is for the log.
 *
 * <p>Scoped to this context's REST package: an adapter answers for its own module only (ADR-037).
 */
@RestControllerAdvice(
    basePackages = "dev.domaincentric.sample.ecommerce.pricing.adapter.incoming.api")
public class PricingApiExceptionHandler {

  private static final Logger LOG = LoggerFactory.getLogger(PricingApiExceptionHandler.class);

  /** Pricing holds no price for the product — there is no such product. */
  @ExceptionHandler(ProductPriceNotFoundException.class)
  public ProblemDetail handlePriceNotFound(final ProductPriceNotFoundException exception) {
    return problem(
        HttpStatus.NOT_FOUND,
        "Product not found",
        "There is no product " + exception.productId().value() + ".");
  }

  /** The body names a value the request refuses; {@code errors} names each field and why. */
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
   * parse in {@link PriceResource} — a malformed call either way. The one mapping that stays
   * imprecise: this is also the type a defect in this application raises, and nothing in the type
   * tells the two apart.
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
