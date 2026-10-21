package dev.domaincentric.sample.ecommerce.checkout.domain.model;

import dev.domaincentric.dca.buildingblocks.ddd.tactical.Value;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.OptionalInt;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * The fingerprint of a checkout review: per line one digest of what is ordered — its product and
 * quantity — and one of its unit price, and one digest of everything else the review shows. "Place
 * Order" carries the fingerprint of the review the customer saw; a review worked out again with
 * another fingerprint is not the order they saw.
 *
 * <p>Written as the lines joined by {@value #LINE_SEPARATOR}, each its item digest, {@value
 * #PRICE_SEPARATOR} and its price digest, then {@value #REST_SEPARATOR} and the digest of the rest.
 * A value not in that form is a fingerprint of no review.
 *
 * @param value the fingerprint as the review page carries it
 */
public record ReviewFingerprint(String value) implements Value {

  private static final String LINE_SEPARATOR = ".";
  private static final String PRICE_SEPARATOR = ":";
  private static final String REST_SEPARATOR = "~";
  private static final int DIGEST_BYTES = 8;
  private static final String DIGEST_FORM = "[0-9a-f]{" + DIGEST_BYTES * 2 + "}";
  private static final String LINE_FORM =
      DIGEST_FORM + Pattern.quote(PRICE_SEPARATOR) + DIGEST_FORM;
  private static final Pattern FORM =
      Pattern.compile(
          LINE_FORM
              + "("
              + Pattern.quote(LINE_SEPARATOR)
              + LINE_FORM
              + ")*"
              + Pattern.quote(REST_SEPARATOR)
              + DIGEST_FORM);

  /**
   * What one line of a review shows, as the fingerprint reads it.
   *
   * @param item the line's product and quantity
   * @param price the line's unit price
   */
  record Line(String item, String price) implements Value {}

  public ReviewFingerprint {
    if (value == null) {
      throw new IllegalArgumentException("Review fingerprint cannot be null");
    }
  }

  public static ReviewFingerprint of(final String value) {
    return new ReviewFingerprint(value);
  }

  /**
   * The fingerprint of a review's content.
   *
   * @param lines each line's item and unit price, in the review's order
   * @param rest everything else the review shows
   * @return the fingerprint
   */
  static ReviewFingerprint digestOf(final List<Line> lines, final String rest) {
    return new ReviewFingerprint(
        lines.stream()
                .map(line -> digest(line.item()) + PRICE_SEPARATOR + digest(line.price()))
                .collect(Collectors.joining(LINE_SEPARATOR))
            + REST_SEPARATOR
            + digest(rest));
  }

  /**
   * The first line whose unit price alone differs from the shown fingerprint's — the same product
   * in the same quantity on every line, at another price on this one.
   *
   * @param shown the fingerprint of the review the customer saw
   * @return the line's position, or empty when no line's price differs, when what is ordered
   *     differs, or when the two cannot be compared line by line
   */
  public OptionalInt firstChangedLine(final ReviewFingerprint shown) {
    if (!isWellFormed() || !shown.isWellFormed()) {
      return OptionalInt.empty();
    }
    final List<String> lines = lines();
    final List<String> shownLines = shown.lines();
    if (lines.size() != shownLines.size()
        || IntStream.range(0, lines.size())
            .anyMatch(
                position ->
                    !itemOf(lines.get(position)).equals(itemOf(shownLines.get(position))))) {
      return OptionalInt.empty();
    }
    return IntStream.range(0, lines.size())
        .filter(position -> !lines.get(position).equals(shownLines.get(position)))
        .findFirst();
  }

  private boolean isWellFormed() {
    return FORM.matcher(value).matches();
  }

  private List<String> lines() {
    return List.of(
        value.substring(0, value.indexOf(REST_SEPARATOR)).split(Pattern.quote(LINE_SEPARATOR)));
  }

  private static String itemOf(final String line) {
    return line.substring(0, line.indexOf(PRICE_SEPARATOR));
  }

  private static String digest(final String content) {
    try {
      final byte[] hash =
          MessageDigest.getInstance("SHA-256").digest(content.getBytes(StandardCharsets.UTF_8));
      return HexFormat.of().formatHex(hash, 0, DIGEST_BYTES);
    } catch (final NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 is not available", e);
    }
  }
}
