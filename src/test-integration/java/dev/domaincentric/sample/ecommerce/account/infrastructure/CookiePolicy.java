package dev.domaincentric.sample.ecommerce.account.infrastructure;

import java.util.List;
import java.util.Locale;

/**
 * One {@code Set-Cookie} header, read as the browser reads it. The servlet {@code Cookie} object
 * drops {@code SameSite}, so the assertions work on the header text.
 */
record CookiePolicy(String name, String sameSite, boolean secure, boolean httpOnly) {

  /** The cookie of that name among the response's {@code Set-Cookie} headers, or {@code null}. */
  static CookiePolicy named(final List<String> setCookieHeaders, final String name) {
    return setCookieHeaders.stream()
        .filter(header -> header.startsWith(name + "="))
        .map(CookiePolicy::parse)
        .findFirst()
        .orElse(null);
  }

  private static CookiePolicy parse(final String header) {
    final String[] attributes = header.split(";");
    String sameSite = null;
    boolean secure = false;
    boolean httpOnly = false;
    for (final String attribute : attributes) {
      final String trimmed = attribute.trim();
      final String lower = trimmed.toLowerCase(Locale.ROOT);
      if (lower.startsWith("samesite=")) {
        sameSite = trimmed.substring("samesite=".length());
      } else if (lower.equals("secure")) {
        secure = true;
      } else if (lower.equals("httponly")) {
        httpOnly = true;
      }
    }
    return new CookiePolicy(attributes[0].split("=", 2)[0], sameSite, secure, httpOnly);
  }
}
