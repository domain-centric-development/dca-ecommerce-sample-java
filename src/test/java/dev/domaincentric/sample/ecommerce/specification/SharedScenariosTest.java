package dev.domaincentric.sample.ecommerce.specification;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

/**
 * The end-user suite implements the scenarios of this shop's specification ({@code
 * product/spec/**}{@code /scenarios.md}): every browser test carries a known scenario's title as
 * its display name, no title names two tests, and every scenario whose {@code Test:} line names a
 * browser test names one that exists. A scenario's title is its {@code Title:} line, else its key
 * in words. A known title is a scenario's of the specification or an acceptance criterion's of a
 * story not yet delivered ({@code product/epics/**}{@code /story.md}, {@code status:} neither
 * {@code delivered} nor {@code superseded}): its scenario reaches the specification only when the
 * gate delivers the story, and its browser test exists before that. The uniqueness of titles and
 * the {@code Test:} lines are checked on the specification alone. Checked against the sources of
 * {@code src/test-e2e}, so it holds without a running shop or a browser, on every build.
 */
class SharedScenariosTest {
  static final Path SPECIFICATION = Path.of("product/spec");
  static final Path EPICS = Path.of("product/epics");

  /** The states of a story whose criteria are no title to come: delivered, or replaced. */
  static final Set<String> SETTLED = Set.of("delivered", "superseded");

  /** The marker after a criterion's key that names the happy path; not part of the key. */
  static final Pattern HAPPY_PATH = Pattern.compile("\\s+\\(happy path\\)\\s*$");

  static final Path E2E_SOURCES = Path.of("src/test-e2e/java");

  /** How a {@code Test:} line names a browser test: {@code package.Class#method}. */
  static final String BROWSER_TEST_PACKAGE = "dev.domaincentric.sample.ecommerce.e2e.";

  /**
   * Browser tests still titled after the shared specification's scenarios, which this shop's
   * specification describes under other titles or not at all. The list only shrinks: a title that
   * becomes a scenario's, or that no browser test carries any more, fails until it is taken off.
   */
  static final Set<String> TITLED_AFTER_THE_SHARED_SPECIFICATION = Set.of();

  /**
   * Titles two browser tests carry, waiting for the decision which one stays. The list only
   * shrinks: a title that one test carries again fails until it is taken off.
   */
  static final Set<String> TITLES_ON_TWO_TESTS = Set.of();

  /**
   * One {@code @DisplayName(...)}, its string possibly split over several literals, each of which
   * may carry escapes ({@code \"}, {@code \\}).
   */
  static final Pattern DISPLAY_NAME =
      Pattern.compile("@DisplayName\\(\\s*((?:\"(?:[^\"\\\\]|\\\\.)*\"\\s*\\+?\\s*)+)\\)");

  static final Pattern LITERAL = Pattern.compile("\"((?:[^\"\\\\]|\\\\.)*)\"");

  /** An escape in a literal: the character after the backslash, as the runtime reads it. */
  static final Pattern ESCAPE = Pattern.compile("\\\\(.)");

  static final Pattern TEST_ANNOTATION = Pattern.compile("@Test\\b");
  static final Pattern PACKAGE = Pattern.compile("^package\\s+([\\w.]+);", Pattern.MULTILINE);

  /** A class declaration at the start of a line, so the word "class" in a comment is not one. */
  static final Pattern CLASS =
      Pattern.compile(
          "^\\s*(?:(?:public|protected|private|abstract|final|static)\\s+)*class\\s+(\\w+)",
          Pattern.MULTILINE);

  static final Pattern METHOD = Pattern.compile("\\bvoid\\s+(\\w+)\\s*\\(");

  /** A scenario: its title and the tests its {@code Test:} lines name. */
  record Scenario(String title, List<String> tests) {}

  @Test
  void everyBrowserTestIsAScenarioOfThisShopsSpecification() throws IOException {
    Set<String> titles = new HashSet<>();
    for (Scenario scenario : scenarios()) titles.add(scenario.title());
    assertFalse(titles.isEmpty(), "product/spec holds no scenario");
    Set<String> known = new HashSet<>(titles);
    known.addAll(storyTitles());

    List<String> bound = new ArrayList<>();
    int tests = 0;
    for (Path source : e2eSources()) {
      String text = Files.readString(source);
      tests += countOf("@Test", text);
      bound.addAll(methodDisplayNames(text));
    }
    assertEquals(tests, bound.size(), "every browser test names its scenario with @DisplayName");

    var unknown = new TreeSet<String>();
    for (String title : bound) {
      if (!known.contains(title) && !TITLED_AFTER_THE_SHARED_SPECIFICATION.contains(title)) {
        unknown.add(title);
      }
    }
    assertEquals(
        Set.of(),
        unknown,
        "browser tests whose title is no scenario of product/spec or of a story not yet delivered");

    var duplicates = new TreeMap<String, Integer>();
    for (String title : bound) duplicates.merge(title, 1, Integer::sum);
    duplicates.values().removeIf(count -> count == 1);
    var undecided = new TreeSet<String>(TITLES_ON_TWO_TESTS);
    undecided.removeAll(duplicates.keySet());
    assertEquals(
        Set.of(), undecided, "titles on two tests that one test carries now: take them off");
    duplicates.keySet().removeAll(TITLES_ON_TWO_TESTS);
    assertEquals(Map.of(), duplicates, "titles that name more than one browser test");

    var stale = new TreeSet<String>();
    for (String title : TITLED_AFTER_THE_SHARED_SPECIFICATION) {
      if (titles.contains(title) || !bound.contains(title)) stale.add(title);
    }
    assertEquals(
        Set.of(),
        stale,
        "exceptions that are a scenario's title now or name no browser test: take them off");
  }

  @Test
  void aStoryNotYetDeliveredNamesItsCriteriaAsScenarios() {
    List<String> story =
        List.of(
            "---",
            "id: a-story",
            "status: approved",
            "---",
            "",
            "# A story",
            "",
            "## Acceptance criteria",
            "",
            "### Rule: one rule",
            "",
            "#### takes-the-line-out (happy path)",
            "- Given a cart",
            "",
            "#### keeps-the-order",
            "Title: The other lines keep their order",
            "- Given a cart",
            "",
            "## Out of scope",
            "",
            "#### not-a-criterion");

    assertEquals("approved", status(story));
    assertEquals(
        List.of("Takes the line out", "The other lines keep their order"),
        storyScenariosIn(story).stream().map(Scenario::title).toList());
  }

  @Test
  void everyBrowserTestAScenarioNamesExists() throws IOException {
    Set<String> browserTests = new HashSet<>();
    for (Path source : e2eSources()) browserTests.addAll(browserTestsIn(Files.readString(source)));

    var missing = new TreeSet<String>();
    for (Scenario scenario : scenarios()) {
      for (String test : scenario.tests()) {
        if (test.startsWith(BROWSER_TEST_PACKAGE) && !browserTests.contains(test)) {
          missing.add(test);
        }
      }
    }
    assertEquals(Set.of(), missing, "Test lines that name a browser test no source holds");
  }

  /**
   * Each scenario's title with the tests its {@code Test:} lines name, read from every {@code
   * scenarios.md} below {@code product/spec}. Two contexts may hold a scenario of the same title;
   * only a browser test's title has to be unique.
   */
  static List<Scenario> scenarios() throws IOException {
    var scenarios = new ArrayList<Scenario>();
    for (Path file : filesNamed(SPECIFICATION, "scenarios.md")) {
      scenarios.addAll(scenariosIn(Files.readAllLines(file)));
    }
    return scenarios;
  }

  /**
   * The titles of the acceptance criteria of every story below {@code product/epics} that is not
   * delivered yet: the scenarios the gate will add to the specification when it delivers the story.
   */
  static List<String> storyTitles() throws IOException {
    var titles = new ArrayList<String>();
    for (Path file : filesNamed(EPICS, "story.md")) {
      List<String> lines = Files.readAllLines(file);
      if (SETTLED.contains(status(lines).toLowerCase(Locale.ROOT))) continue;
      for (Scenario scenario : storyScenariosIn(lines)) titles.add(scenario.title());
    }
    return titles;
  }

  /** A story's {@code status:} from its front matter, empty without one. */
  static String status(List<String> lines) {
    if (lines.isEmpty() || !lines.get(0).trim().equals("---")) return "";
    for (String line : lines.subList(1, lines.size())) {
      if (line.trim().equals("---")) break;
      if (line.startsWith("status:")) return line.substring(7).trim();
    }
    return "";
  }

  /**
   * The scenarios of a story's {@code ## Acceptance criteria} section, read like a {@code
   * scenarios.md}; a key's trailing {@code (happy path)} marker is not part of it.
   */
  static List<Scenario> storyScenariosIn(List<String> lines) {
    var criteria = new ArrayList<String>();
    boolean inCriteria = false;
    for (String line : lines) {
      if (line.startsWith("## ")) {
        inCriteria = line.startsWith("## Acceptance criteria");
      } else if (inCriteria) {
        criteria.add(line.startsWith("#### ") ? HAPPY_PATH.matcher(line).replaceAll("") : line);
      }
    }
    return scenariosIn(criteria);
  }

  /**
   * The scenarios of one document: a {@code #### key} heading, an optional Title line, the Test
   * lines; a {@code ###} heading ends a scenario.
   */
  static List<Scenario> scenariosIn(List<String> document) {
    var scenarios = new ArrayList<Scenario>();
    String key = null;
    String title = null;
    List<String> tests = new ArrayList<>();
    var lines = new ArrayList<>(document);
    lines.add("#### ");
    for (String line : lines) {
      if (line.startsWith("#### ") || line.startsWith("### ")) {
        if (key != null) scenarios.add(new Scenario(title != null ? title : inWords(key), tests));
        key = line.startsWith("#### ") && line.length() > 5 ? line.substring(5).trim() : null;
        title = null;
        tests = new ArrayList<>();
      } else if (key != null && line.startsWith("Title:")) {
        title = line.substring(6).trim();
      } else if (key != null && line.startsWith("Test:")) {
        Arrays.stream(line.substring(5).split("[ ,;]+"))
            .filter(name -> !name.isEmpty())
            .forEach(tests::add);
      }
    }
    return scenarios;
  }

  /** The files of one name below a root, which may be a link (a story worktree links the epics). */
  static List<Path> filesNamed(Path root, String name) throws IOException {
    try (Stream<Path> walk = Files.walk(root, FileVisitOption.FOLLOW_LINKS)) {
      return walk.filter(p -> p.getFileName().toString().equals(name)).sorted().toList();
    }
  }

  /**
   * A scenario's key in words, the title it has without a Title line: "shows-empty-state" is "Shows
   * empty state".
   */
  static String inWords(String key) {
    return Character.toUpperCase(key.charAt(0)) + key.substring(1).replace('-', ' ');
  }

  /** The browser tests of one source as {@code package.Class#method}, as a Test line names them. */
  static List<String> browserTestsIn(String text) {
    Matcher pkg = PACKAGE.matcher(text);
    String prefix = pkg.find() ? pkg.group(1) + "." : "";
    var tests = new ArrayList<String>();
    Matcher annotation = TEST_ANNOTATION.matcher(text);
    while (annotation.find()) {
      String className = null;
      Matcher declaration = CLASS.matcher(text.substring(0, annotation.start()));
      while (declaration.find()) className = declaration.group(1);
      Matcher method = METHOD.matcher(text);
      String methodName = method.find(annotation.end()) ? method.group(1) : null;
      tests.add(prefix + className + "#" + methodName);
    }
    return tests;
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
      while (literal.find()) literals.append(unescaped(literal.group(1)));
      names.add(literals.toString());
    }
    return names;
  }

  /** A literal's content as the runtime sees it: {@code \"} is a quote, {@code \\} a backslash. */
  static String unescaped(String literal) {
    return ESCAPE.matcher(literal).replaceAll(escape -> Matcher.quoteReplacement(escape.group(1)));
  }

  static int countOf(String token, String text) {
    return (int) Pattern.compile(Pattern.quote(token) + "\\b").matcher(text).results().count();
  }
}
