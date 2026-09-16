package dev.domaincentric.sample.ecommerce.account.adapter.incoming.api;

import dev.domaincentric.sample.ecommerce.account.application.authenticateaccount.AuthenticateAccountCommand;
import dev.domaincentric.sample.ecommerce.account.application.authenticateaccount.AuthenticateAccountInputPort;
import dev.domaincentric.sample.ecommerce.account.application.authenticateaccount.AuthenticateAccountResult;
import dev.domaincentric.sample.ecommerce.account.application.registeraccount.RegisterAccountCommand;
import dev.domaincentric.sample.ecommerce.account.application.registeraccount.RegisterAccountInputPort;
import dev.domaincentric.sample.ecommerce.account.application.registeraccount.RegisterAccountResult;
import dev.domaincentric.sample.ecommerce.account.application.shared.TokenService;
import dev.domaincentric.sample.ecommerce.sharedkernel.application.shared.IdentityProvider;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.UserId;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
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
  public ResponseEntity<LoginResponse> login(@Valid @RequestBody final LoginRequest request) {

    final AuthenticateAccountCommand command =
        new AuthenticateAccountCommand(request.email(), request.password());

    final AuthenticateAccountResult result = authenticateAccountUseCase.execute(command);

    if (!result.success()) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(LoginResponse.failure(result.errorMessage()));
    }

    final String token =
        tokenService.generateRegisteredToken(
            UserId.of(result.userId()), result.email(), result.roles());

    return ResponseEntity.ok(LoginResponse.success(token, result.email()));
  }

  @PostMapping("/register")
  public ResponseEntity<RegisterResponse> register(
      @Valid @RequestBody final RegisterRequest request) {

    final String currentUserId = identityProvider.getCurrentIdentity().userId().value();

    final RegisterAccountCommand command =
        new RegisterAccountCommand(
            request.email(),
            request.password(),
            currentUserId,
            request.firstName(),
            request.lastName(),
            request.dateOfBirth());

    try {
      final RegisterAccountResult result = registerAccountUseCase.execute(command);

      final String token =
          tokenService.generateRegisteredToken(
              UserId.of(result.userId()), result.email(), result.roles());

      return ResponseEntity.status(HttpStatus.CREATED)
          .body(RegisterResponse.success(token, result.email()));

    } catch (final IllegalArgumentException e) {
      return ResponseEntity.badRequest().body(RegisterResponse.failure(e.getMessage()));
    }
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
