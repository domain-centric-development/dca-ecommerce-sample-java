package dev.domaincentric.sample.ecommerce.e2e;

import java.util.regex.Pattern;

/**
 * The address of a page, whatever notice a redirect added to it. A redirect carries its message as
 * a query ({@code ?notice=<code>}), so a test that asks which page is open compares the path. The
 * pattern is spelled so that Playwright hands it to the browser unchanged: no Java-only quoting.
 */
public final class PageAddress {

  private PageAddress() {}

  /**
   * A pattern for the page at that address, with or without a query.
   *
   * @param glob the address; {@code *} stands for one path segment's characters, {@code **} for
   *     anything
   * @return the pattern
   */
  public static Pattern of(final String glob) {
    final StringBuilder regex = new StringBuilder("^");
    int i = 0;
    while (i < glob.length()) {
      if (glob.startsWith("**", i)) {
        regex.append(".*");
        i += 2;
      } else if (glob.charAt(i) == '*') {
        regex.append("[^/?]*");
        i++;
      } else {
        final int next = nextWildcard(glob, i);
        regex.append(escaped(glob.substring(i, next)));
        i = next;
      }
    }
    return Pattern.compile(regex.append("(\\?.*)?$").toString());
  }

  /** Escapes every character a regular expression gives a meaning, the way the browser reads it. */
  private static String escaped(final String literal) {
    return literal.replaceAll("[\\\\^$.|?*+()\\[\\]{}/-]", "\\\\$0");
  }

  private static int nextWildcard(final String glob, final int from) {
    final int star = glob.indexOf('*', from);
    return star < 0 ? glob.length() : star;
  }
}
