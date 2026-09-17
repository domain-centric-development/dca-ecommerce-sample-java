package dev.domaincentric.sample.ecommerce.account.adapter.outgoing.security;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

/**
 * Renders an API refusal as an RFC 9457 problem document. Writing a body here also keeps the
 * servlet container from forwarding the request onto the HTML error page.
 */
final class ApiRefusal {

  private ApiRefusal() {}

  static void write(final HttpServletResponse response, final HttpStatus status)
      throws IOException {

    response.setStatus(status.value());
    response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
    response
        .getWriter()
        .write(
            "{\"type\":\"about:blank\",\"title\":\"%s\",\"status\":%d}"
                .formatted(status.getReasonPhrase(), status.value()));
  }
}
