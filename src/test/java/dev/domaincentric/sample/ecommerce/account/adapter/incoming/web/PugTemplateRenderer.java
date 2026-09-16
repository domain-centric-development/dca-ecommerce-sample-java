package dev.domaincentric.sample.ecommerce.account.adapter.incoming.web;

import de.neuland.pug4j.PugEngine;
import de.neuland.pug4j.template.ClasspathTemplateLoader;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.UserId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 * Test helper that renders a production Pug template from the classpath and parses the result.
 *
 * <p>Renders through {@code layout.pug} exactly as the running application does. The baseline model
 * mirrors the four attributes {@code MiniBasketControllerAdvice} contributes to every request —
 * {@code miniBasketItemCount}, {@code miniBasketTotal}, {@code miniBasketItems} and {@code
 * identity} — so a template test fails for template reasons only, not for missing layout variables.
 *
 * <p>The baseline {@code identity} is a registered one: an account page is only ever reached by a
 * logged-in user, so the layout renders its registered header branch as it does in production.
 */
final class PugTemplateRenderer {

  private static final PugEngine ENGINE = createEngine();

  private PugTemplateRenderer() {}

  /**
   * Renders the given view name with the baseline layout model plus the given attributes.
   *
   * @param viewName the view name, e.g. {@code account/change-password}
   * @param attributes the page-specific model attributes
   * @return the parsed rendered document
   */
  static Document render(final String viewName, final Map<String, Object> attributes) {
    final Map<String, Object> model = new HashMap<>(baselineModel());
    model.putAll(attributes);
    try {
      return Jsoup.parse(ENGINE.render(ENGINE.getTemplate(viewName), model));
    } catch (final Exception e) {
      throw new AssertionError(
          "Could not render template '" + viewName + "': " + e.getMessage(), e);
    }
  }

  private static Map<String, Object> baselineModel() {
    final Map<String, Object> model = new HashMap<>();
    model.put("miniBasketItemCount", 0);
    model.put("miniBasketTotal", "");
    model.put("miniBasketItems", List.of());
    model.put(
        "identity",
        AccountWebTestFixtures.TestIdentity.registered(
            UserId.of("user-4711"), "jane.doe@example.com"));
    return model;
  }

  private static PugEngine createEngine() {
    return PugEngine.builder()
        .templateLoader(new ClasspathTemplateLoader("templates/"))
        .caching(false)
        .build();
  }
}
