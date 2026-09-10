package dev.domaincentric.sample.ecommerce.cart.domain.model;

import dev.domaincentric.dca.buildingblocks.ddd.tactical.Value;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Immutable compressed identities of units in one cart position; reductions remove oldest units
 * first.
 */
public record PositionUnits(long allocated, List<Span> spans) implements Value {
  public record Span(long first, long last) implements Value {
    public Span {
      if (first < 1 || last < first) throw new IllegalArgumentException("Invalid unit interval");
    }
  }

  public PositionUnits {
    spans = List.copyOf(spans);
    long previous = 0;
    for (Span span : spans) {
      if (span.first() <= previous || span.last() > allocated)
        throw new IllegalArgumentException("Invalid unit sequence");
      previous = span.last();
    }
  }

  public static PositionUnits initial(int quantity) {
    if (quantity <= 0) throw new IllegalArgumentException("Quantity must be positive");
    return new PositionUnits(quantity, List.of(new Span(1, quantity)));
  }

  public int quantity() {
    return Math.toIntExact(spans.stream().mapToLong(s -> s.last() - s.first() + 1).sum());
  }

  public PositionUnits resize(int quantity) {
    if (quantity <= 0) throw new IllegalArgumentException("Quantity must be positive");
    int delta = quantity - quantity();
    if (delta == 0) return this;
    if (delta > 0) {
      var result = new ArrayList<>(spans);
      result.add(new Span(Math.addExact(allocated, 1), Math.addExact(allocated, delta)));
      return new PositionUnits(Math.addExact(allocated, delta), result);
    }
    long remove = -((long) delta);
    var result = new ArrayList<Span>();
    for (Span span : spans) {
      long take = Math.min(remove, span.last() - span.first() + 1);
      remove -= take;
      if (span.first() + take <= span.last())
        result.add(new Span(span.first() + take, span.last()));
    }
    return new PositionUnits(allocated, result);
  }

  public PositionUnits reconcile(PositionUnits purchased) {
    var result = new ArrayList<Span>();
    for (Span current : spans) {
      long cursor = current.first();
      for (Span bought : purchased.spans()) {
        if (bought.last() < cursor || bought.first() > current.last()) continue;
        if (bought.first() > cursor) result.add(new Span(cursor, bought.first() - 1));
        cursor = Math.max(cursor, bought.last() + 1);
        if (cursor > current.last()) break;
      }
      if (cursor <= current.last()) result.add(new Span(cursor, current.last()));
    }
    return new PositionUnits(allocated, result);
  }

  public String serialize() {
    return allocated
        + "|"
        + spans.stream().map(s -> s.first() + "-" + s.last()).collect(Collectors.joining(","));
  }

  public static PositionUnits parse(String encoded) {
    String[] parts = encoded.split("\\|", -1);
    if (parts.length != 2) throw new IllegalArgumentException("Invalid unit snapshot");
    var spans = new ArrayList<Span>();
    if (!parts[1].isEmpty())
      for (String token : parts[1].split(",")) {
        String[] pair = token.split("-");
        spans.add(new Span(Long.parseLong(pair[0]), Long.parseLong(pair[1])));
      }
    return new PositionUnits(Long.parseLong(parts[0]), spans);
  }
}
