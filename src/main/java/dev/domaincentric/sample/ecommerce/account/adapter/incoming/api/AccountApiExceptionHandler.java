package dev.domaincentric.sample.ecommerce.account.adapter.incoming.api;

import dev.domaincentric.dca.buildingblocks.ddd.tactical.DomainException;
import dev.domaincentric.sample.ecommerce.account.domain.model.AccountClosedException;
import dev.domaincentric.sample.ecommerce.account.domain.model.AccountNotAccessibleException;
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
 * Translates what is still <em>thrown</em> towards the account context's REST package into HTTP
 * answers.
 *
 * <p>The account's use cases return every refusal a caller answers as a case of their result — a
 * wrong password, an address somebody else holds, a password the model will not accept — and {@link
 * AuthResource} switches over it. What reaches this handler is what no use case returns: a rule of
 * the model no use case names as a case of its own (an account that is closed), a request the
 * binding refuses and a value that does not parse. The words are shared with the resource through
 * {@link AccountProblems}.
 *
 * <p>The words are this adapter's own: a detail is built from the failure's values, never from its
 * message, which is for the log.
 *
 * <p>Scoped to this context's REST package: an adapter answers for its own module only (ADR-037).
 */
@RestControllerAdvice(
    basePackages = "dev.domaincentric.sample.ecommerce.account.adapter.incoming.api")
public class AccountApiExceptionHandler {

  private static final Logger LOG = LoggerFactory.getLogger(AccountApiExceptionHandler.class);

  private static final String CANNOT_SIGN_IN = "This account cannot sign in.";

  /** The account exists but is closed. */
  @ExceptionHandler(AccountClosedException.class)
  public ProblemDetail handleClosedAccount(final AccountClosedException exception) {
    return AccountProblems.problem(
        HttpStatus.CONFLICT,
        "Account closed",
        "Account " + exception.accountId().value() + " is closed.");
  }

  /**
   * The account's status does not allow signing in. The status itself is deliberately not in the
   * answer: it would tell a stranger which addresses have suspended accounts.
   */
  @ExceptionHandler(AccountNotAccessibleException.class)
  public ProblemDetail handleInaccessibleAccount(final AccountNotAccessibleException exception) {
    LOG.info(
        "Sign-in refused for account {}: {}", exception.accountId().value(), exception.status());
    return AccountProblems.problem(HttpStatus.FORBIDDEN, "Account not accessible", CANNOT_SIGN_IN);
  }

  /** A rule of the model refused the request. */
  @ExceptionHandler(DomainException.class)
  public ProblemDetail handleDomainRule(final DomainException exception) {
    return AccountProblems.problem(HttpStatus.UNPROCESSABLE_ENTITY, "Business rule violated");
  }

  /**
   * A request body of this package fails its own validation — a date of birth that does not lie
   * before today, for one; {@code errors} names each refused field as the request spells it, and
   * why.
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ProblemDetail handleRefusedField(final MethodArgumentNotValidException exception) {
    final Map<String, String> errors = new LinkedHashMap<>();
    for (final FieldError error : exception.getBindingResult().getFieldErrors()) {
      errors.putIfAbsent(error.getField(), error.getDefaultMessage());
    }
    final ProblemDetail problem = AccountProblems.unacceptableValue();
    problem.setProperty("errors", errors);
    return problem;
  }

  /**
   * A value the caller sent is not acceptable to a value object — a malformed address, a blank
   * name. The one mapping that stays imprecise: this is also the type a defect in this application
   * raises, and nothing in the type tells the two apart.
   */
  @ExceptionHandler(IllegalArgumentException.class)
  public ProblemDetail handleUnacceptableValue(final IllegalArgumentException exception) {
    LOG.debug("Rejected request value: {}", exception.getMessage());
    return AccountProblems.unacceptableValue();
  }
}
