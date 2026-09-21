package dev.domaincentric.sample.ecommerce.account.adapter.incoming.api;

import dev.domaincentric.dca.buildingblocks.application.UseCaseException;
import dev.domaincentric.dca.buildingblocks.ddd.tactical.DomainException;
import dev.domaincentric.sample.ecommerce.account.application.registeraccount.AccountAlreadyExistsException;
import dev.domaincentric.sample.ecommerce.account.application.registeraccount.EmailAlreadyRegisteredException;
import dev.domaincentric.sample.ecommerce.account.domain.model.AccountClosedException;
import dev.domaincentric.sample.ecommerce.account.domain.model.AccountNotAccessibleException;
import dev.domaincentric.sample.ecommerce.account.domain.model.PasswordTooWeakException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Translates the account context's failures into HTTP answers — the one place in this context that
 * knows the protocol.
 *
 * <p>Authentication itself does not arrive here: a wrong password is an outcome of the
 * authentication use case, returned as a value, and the resource renders it. What arrives are the
 * refusals: an address somebody else holds, a password the model will not accept, an account that
 * is closed.
 *
 * <p>Scoped to this context's REST package: an adapter answers for its own module only (ADR-037).
 */
@RestControllerAdvice(
    basePackages = "dev.domaincentric.sample.ecommerce.account.adapter.incoming.api")
public class AccountApiExceptionHandler {

  private static final Logger LOG = LoggerFactory.getLogger(AccountApiExceptionHandler.class);

  /** Another account already holds that address. */
  @ExceptionHandler(EmailAlreadyRegisteredException.class)
  public ProblemDetail handleEmailTaken(final EmailAlreadyRegisteredException exception) {
    return problem(HttpStatus.CONFLICT, "Email already registered", exception.getMessage());
  }

  /** The signed-in user already has an account. */
  @ExceptionHandler(AccountAlreadyExistsException.class)
  public ProblemDetail handleAccountExists(final AccountAlreadyExistsException exception) {
    return problem(HttpStatus.CONFLICT, "Account already exists", exception.getMessage());
  }

  /**
   * The password does not meet the strength rules. The model's own wording goes to the caller
   * verbatim — it says what to change, and it describes the rule, not the attempt.
   */
  @ExceptionHandler(PasswordTooWeakException.class)
  public ProblemDetail handleWeakPassword(final PasswordTooWeakException exception) {
    return problem(HttpStatus.UNPROCESSABLE_ENTITY, "Password too weak", exception.getMessage());
  }

  /** The account exists but is closed. */
  @ExceptionHandler(AccountClosedException.class)
  public ProblemDetail handleClosedAccount(final AccountClosedException exception) {
    return problem(HttpStatus.CONFLICT, "Account closed", exception.getMessage());
  }

  /**
   * The account's status does not allow signing in. The status itself is deliberately not in the
   * answer: it would tell a stranger which addresses have suspended accounts.
   */
  @ExceptionHandler(AccountNotAccessibleException.class)
  public ProblemDetail handleInaccessibleAccount(final AccountNotAccessibleException exception) {
    LOG.info(
        "Sign-in refused for account {}: {}", exception.accountId().value(), exception.status());
    return problem(HttpStatus.FORBIDDEN, "Account not accessible", "This account cannot sign in");
  }

  /** Any other failure the use case reports. */
  @ExceptionHandler(UseCaseException.class)
  public ProblemDetail handleUseCaseFailure(final UseCaseException exception) {
    return problem(
        HttpStatus.UNPROCESSABLE_ENTITY, "Request cannot be served", exception.getMessage());
  }

  /** A rule of the model refused the request. */
  @ExceptionHandler(DomainException.class)
  public ProblemDetail handleDomainRule(final DomainException exception) {
    return problem(
        HttpStatus.UNPROCESSABLE_ENTITY, "Business rule violated", exception.getMessage());
  }

  /**
   * A value the caller sent is not acceptable to a value object — a malformed address, a blank
   * name. The one mapping that stays imprecise: this is also the type a defect in this application
   * raises, and nothing in the type tells the two apart.
   */
  @ExceptionHandler(IllegalArgumentException.class)
  public ProblemDetail handleUnacceptableValue(final IllegalArgumentException exception) {
    LOG.debug("Rejected request value: {}", exception.getMessage());
    return problem(HttpStatus.BAD_REQUEST, "Unacceptable value", exception.getMessage());
  }

  private static ProblemDetail problem(
      final HttpStatus status, final String title, final String detail) {
    final ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
    problem.setTitle(title);
    return problem;
  }
}
