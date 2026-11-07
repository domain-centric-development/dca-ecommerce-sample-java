package dev.domaincentric.sample.ecommerce.account.adapter.incoming.api;

import dev.domaincentric.sample.ecommerce.account.domain.model.Email;
import dev.domaincentric.sample.ecommerce.account.domain.model.HashedPassword;
import dev.domaincentric.sample.ecommerce.account.domain.model.PasswordRule;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

/**
 * The HTTP answers of the account context's refusals — the words this context's REST package uses
 * for them.
 *
 * <p>{@link AuthResource} switches over each use case's result and takes the answer for a refusal
 * from here; {@link AccountApiExceptionHandler} takes the answers for what is still thrown. A
 * detail is built from the refusal's values, never from a message.
 */
final class AccountProblems {

  private static final String ACCOUNT_EXISTS = "The signed-in user already has an account.";

  private AccountProblems() {}

  /** Another account already holds that address. */
  static ResponseEntity<ProblemDetail> emailAlreadyRegistered(final Email email) {
    return answer(
        problem(
            HttpStatus.CONFLICT,
            "Email already registered",
            "The email address " + email.value() + " is already registered."));
  }

  /** The signed-in user already has an account. */
  static ResponseEntity<ProblemDetail> accountAlreadyExists() {
    return answer(problem(HttpStatus.CONFLICT, "Account already exists", ACCOUNT_EXISTS));
  }

  /**
   * The password does not meet the strength rules. The answer names the broken rule — the rule,
   * never the attempt.
   */
  static ResponseEntity<ProblemDetail> passwordTooWeak(final PasswordRule rule) {
    return answer(
        problem(HttpStatus.UNPROCESSABLE_ENTITY, "Password too weak", passwordRuleSentence(rule)));
  }

  /** A value the caller sent that the request refuses. */
  static ProblemDetail unacceptableValue() {
    return problem(HttpStatus.BAD_REQUEST, "Unacceptable value");
  }

  static ProblemDetail problem(final HttpStatus status, final String title, final String detail) {
    final ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
    problem.setTitle(title);
    return problem;
  }

  /** A refusal the title names completely: no detail beyond it. */
  static ProblemDetail problem(final HttpStatus status, final String title) {
    final ProblemDetail problem = ProblemDetail.forStatus(status);
    problem.setTitle(title);
    return problem;
  }

  private static ResponseEntity<ProblemDetail> answer(final ProblemDetail problem) {
    return ResponseEntity.status(problem.getStatus()).body(problem);
  }

  private static String passwordRuleSentence(final PasswordRule rule) {
    return switch (rule) {
      case TOO_SHORT ->
          "Password must be at least " + HashedPassword.MIN_LENGTH + " characters long.";
      case TOO_LONG ->
          "Password must not be longer than " + HashedPassword.MAX_BYTE_LENGTH + " bytes.";
      case NO_UPPERCASE_LETTER -> "Password must contain an uppercase letter.";
      case NO_LOWERCASE_LETTER -> "Password must contain a lowercase letter.";
      case NO_DIGIT -> "Password must contain a digit.";
    };
  }
}
