package dev.domaincentric.sample.ecommerce.product.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.ProductId;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;

class ProductSelectionTest {

  @Test
  void drawsEightDifferentProductsFromTheCandidates() {
    final List<ProductId> candidates = candidates(21);

    final ProductSelection selection = ProductSelection.draw(candidates, new Random(1));

    assertThat(selection.productIds()).hasSize(ProductSelection.MAX_SIZE).doesNotHaveDuplicates();
    assertThat(candidates).containsAll(selection.productIds());
  }

  @Test
  void drawsEveryCandidateWhenThereAreFewerThanEight() {
    final List<ProductId> candidates = candidates(2);

    final ProductSelection selection = ProductSelection.draw(candidates, new Random(1));

    assertThat(selection.productIds()).containsExactlyInAnyOrderElementsOf(candidates);
  }

  @Test
  void drawsNothingWithoutCandidates() {
    final ProductSelection selection = ProductSelection.draw(List.of(), new Random(1));

    assertThat(selection.productIds()).isEmpty();
  }

  @Test
  void aCandidateNamedTwiceIsDrawnOnce() {
    final List<ProductId> distinct = candidates(3);
    final List<ProductId> twice = new ArrayList<>(distinct);
    twice.addAll(distinct);

    final ProductSelection selection = ProductSelection.draw(twice, new Random(1));

    assertThat(selection.productIds()).containsExactlyInAnyOrderElementsOf(distinct);
  }

  @Test
  void differentRandomSourcesDrawDifferentSelections() {
    final List<ProductId> candidates = candidates(21);
    final Set<Set<ProductId>> selections = new HashSet<>();

    for (int seed = 1; seed <= 20; seed++) {
      selections.add(Set.copyOf(ProductSelection.draw(candidates, new Random(seed)).productIds()));
    }

    assertThat(selections)
        .as("twenty different random sources all drew the same eight products")
        .hasSizeGreaterThan(1);
  }

  @Test
  void theSameRandomSourceDrawsTheSameSelection() {
    final List<ProductId> candidates = candidates(21);

    final ProductSelection first = ProductSelection.draw(candidates, new Random(42));
    final ProductSelection second = ProductSelection.draw(candidates, new Random(42));

    assertThat(first.productIds()).isEqualTo(second.productIds());
  }

  private static List<ProductId> candidates(final int count) {
    return IntStream.range(0, count).mapToObj(i -> ProductId.generate()).toList();
  }
}
