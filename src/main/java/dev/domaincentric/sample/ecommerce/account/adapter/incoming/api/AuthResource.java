package dev.domaincentric.sample.ecommerce.account.adapter.incoming.api;

import dev.domaincentric.sample.ecommerce.account.adapter.incoming.security.TokenService;
import dev.domaincentric.sample.ecommerce.account.application.authenticateaccount.AuthenticateAccountCommand;
import dev.domaincentric.sample.ecommerce.account.application.authenticateaccount.AuthenticateAccountInputPort;
import dev.domaincentric.sample.ecommerce.account.application.authenticateaccount.AuthenticateAccountResult;
import dev.domaincentric.sample.ecommerce.account.application.registeraccount.RegisterAccountCommand;
import dev.domaincentric.sample.ecommerce.account.application.registeraccount.RegisterAccountInputPort;
import dev.domaincentric.sample.ecommerce.account.application.registeraccount.RegisterAccountResult;
import dev.domaincentric.sample.ecommerce.sharedkernel.application.shared.IdentityProvider;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.UserId;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST API Resource for Authentication operations.
 *
 * <p>This is a primary adapter (incoming) in Hexagonal Architecture that exposes authentication
 * functionality via REST API.
 *
 * <p>Endpoints:
 *
 * <ul>
 *   <li>POST /api/auth/login - Authenticate user and return JWT token
 *   <li>POST /api/auth/register - Register new user and return JWT token
 *   <li>POST /api/auth/logout - No-op acknowledgement; the client discards its token
 * </ul>
 *
 * <p><b>Refusals:</b> each route switches over its use case's result — no {@code default}, so the
 * compiler holds that every refusal has an answer — and takes the answer from {@link
 * AccountProblems}. What is still thrown is {@link AccountApiExceptionHandler}'s.
 *
 * <p><b>Stateless by design:</b> the API never sets or reads cookies. Clients send the returned
 * token as {@code Authorization: Bearer}; browser sessions are established through the web login
 * form instead (ADR-035).
 */
@RestController
@RequestMapping("/api/auth")
public class AuthResource {

  private final AuthenticateAccountInputPort authenticateAccountUseCase;
  private final RegisterAccountInputPort registerAccountUseCase;
  private final TokenService tokenService;
  private final IdentityProvider identityProvider;

  public AuthResource(
      final AuthenticateAccountInputPort authenticateAccountUseCase,
      final RegisterAccountInputPort registerAccountUseCase,
      final TokenService tokenService,
      final IdentityProvider identityProvider) {
    this.authenticateAccountUseCase = authenticateAccountUseCase;
    this.registerAccountUseCase = registerAccountUseCase;
    this.tokenService = tokenService;
    this.identityProvider = identityProvider;
  }

  @PostMapping("/login")
  public ResponseEntity<?> login(@Valid @RequestBody final LoginRequest request) {

    final AuthenticateAccountCommand command =
        new AuthenticateAccountCommand(request.email(), request.password());

    return switch (authenticateAccountUseCase.execute(command)) {
      case AuthenticateAccountResult.Authenticated account -> {
        final String token =
            tokenService.generateRegisteredToken(
                UserId.of(account.userId()), account.email(), account.roles());
        yield ResponseEntity.ok(LoginResponse.success(token, account.email()));
      }
      case AuthenticateAccountResult.CredentialsRefused refusal -> refusedLogin();
      case AuthenticateAccountResult.AccountNotAccessible refusal -> refusedLogin();
    };
  }

  /**
   * A refused login is a problem document like every other refusal of the API, with the scheme the
   * caller should authenticate with. It names no reason: an unknown address and a wrong password
   * answer alike.
   */
  private static ResponseEntity<ProblemDetail> refusedLogin() {
    final ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED);
    problem.setTitle("Authentication failed");
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .header(HttpHeaders.WWW_AUTHENTICATE, "Bearer")
        .contentType(MediaType.APPLICATION_PROBLEM_JSON)
        .body(problem);
  }

  @PostMapping("/register")
  public ResponseEntity<?> register(@Valid @RequestBody final RegisterRequest request) {

    final String currentUserId = identityProvider.getCurrentIdentity().userId().value();

    final RegisterAccountCommand command =
        new RegisterAccountCommand(
            request.email(),
            request.password(),
            currentUserId,
            request.firstName(),
            request.lastName(),
            request.dateOfBirth());

    return switch (registerAccountUseCase.execute(command)) {
      case RegisterAccountResult.Registered account -> {
        final String token =
            tokenService.generateRegisteredToken(
                UserId.of(account.userId()), account.email(), account.roles());
        yield ResponseEntity.status(HttpStatus.CREATED)
            .body(RegisterResponse.success(token, account.email()));
      }
      case RegisterAccountResult.EmailAlreadyRegistered refusal ->
          AccountProblems.emailAlreadyRegistered(refusal.email());
      case RegisterAccountResult.AccountAlreadyExists refusal ->
          AccountProblems.accountAlreadyExists();
      case RegisterAccountResult.PasswordTooWeak refusal ->
          AccountProblems.passwordTooWeak(refusal.brokenRule());
    };
  }

  /**
   * Stateless: the API holds no session, so logging out means the client discards its token. The
   * endpoint exists so clients have a uniform place to call; it returns 204.
   */
  @PostMapping("/logout")
  public ResponseEntity<Void> logout() {
    return ResponseEntity.noContent().build();
  }
}
