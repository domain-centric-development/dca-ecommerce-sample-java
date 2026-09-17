package dev.domaincentric.sample.ecommerce.specification;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

/**
 * The end-user suite implements the shared scenarios ({@code scenarios.md} in the specification)
 * and nothing else: every scenario title is the display name of exactly one browser test, and every
 * browser test carries a scenario title. Checked against the sources of {@code src/test-e2e}, so it
 * holds without a running shop or a browser. Runs only when the build was given the local
 * specification checkout ({@code -Pspecification.path=../dca-sample-specification}).
 */
@org.junit.jupiter.api.condition.EnabledIfSystemProperty(
    named = "specification.path",
    matches = ".+",
    disabledReason = "shared specification not supplied (-Pspecification.path)")
class SharedScenariosTest {
  static final Path E2E_SOURCES = Path.of("src/test-e2e/java");

  /** One {@code @DisplayName(...)}, its string possibly split over several literals. */
  static final Pattern DISPLAY_NAME =
      Pattern.compile("@DisplayName\\(\\s*((?:\"[^\"]*\"\\s*\\+?\\s*)+)\\)");

  static final Pattern LITERAL = Pattern.compile("\"([^\"]*)\"");

  @Test
  void everyScenarioIsOneBrowserTestAndEveryBrowserTestIsAScenario() throws IOException {
    Map<String, String> scenarios = scenarioTitles();
    assertFalse(scenarios.isEmpty(), "scenarios.md lists no scenario");

    List<String> bound = new ArrayList<>();
    int tests = 0;
    for (Path source : e2eSources()) {
      String text = Files.readString(source);
      tests += countOf("@Test", text);
      bound.addAll(methodDisplayNames(text));
    }
    assertEquals(tests, bound.size(), "every browser test names its scenario with @DisplayName");

    var missing = new ArrayList<>(scenarios.keySet());
    missing.removeAll(bound);
    assertEquals(List.of(), missing, "scenarios without a browser test in this suite");

    var unknown = new ArrayList<>(bound);
    unknown.removeAll(scenarios.keySet());
    assertEquals(List.of(), unknown, "browser tests whose title is not a shared scenario");

    var duplicates = new TreeMap<String, Integer>();
    for (String title : bound) duplicates.merge(title, 1, Integer::sum);
    duplicates.values().removeIf(count -> count == 1);
    assertEquals(Map.of(), duplicates, "scenarios bound to more than one browser test");
  }

  /**
   * Scenario title by id, read from {@code scenarios.md}: a {@code ## id} heading, then a Title
   * line.
   */
  static Map<String, String> scenarioTitles() throws IOException {
    var titles = new TreeMap<String, String>();
    String id = null;
    for (String line : Files.readAllLines(SharedSpecificationTest.ROOT.resolve("scenarios.md"))) {
      if (line.startsWith("## ")) id = line.substring(3).trim();
      else if (line.startsWith("Title:") && id != null) {
        assertNull(titles.put(line.substring(6).trim(), id), "duplicate scenario title: " + line);
        id = null;
      }
    }
    return titles;
  }

  static List<Path> e2eSources() throws IOException {
    try (Stream<Path> files = Files.walk(E2E_SOURCES)) {
      return files.filter(p -> p.getFileName().toString().endsWith("E2ETest.java")).toList();
    }
  }

  /** Display names of test methods; a class-level {@code @DisplayName} precedes {@code class}. */
  static List<String> methodDisplayNames(String text) {
    var names = new ArrayList<String>();
    Matcher matcher = DISPLAY_NAME.matcher(text);
    while (matcher.find()) {
      String following =
          text.substring(matcher.end(), Math.min(text.length(), matcher.end() + 400));
      String upToBody = following.split("[{;]", 2)[0];
      if (upToBody.matches("(?s).*\\bclass\\b.*")) continue;
      var literals = new StringBuilder();
      Matcher literal = LITERAL.matcher(matcher.group(1));
      while (literal.find()) literals.append(literal.group(1));
      names.add(literals.toString());
    }
    return names;
  }

  static int countOf(String token, String text) {
    return (int) Pattern.compile(Pattern.quote(token) + "\\b").matcher(text).results().count();
  }
}
