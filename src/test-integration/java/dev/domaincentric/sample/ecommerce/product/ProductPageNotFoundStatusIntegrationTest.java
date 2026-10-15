package dev.domaincentric.sample.ecommerce.product;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import dev.domaincentric.sample.ecommerce.infrastructure.EcommerceSampleApplication;
import dev.domaincentric.sample.ecommerce.product.application.getproductbyid.GetProductByIdInputPort;
import dev.domaincentric.sample.ecommerce.product.application.getproductbyid.GetProductByIdResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.util.HtmlUtils;

/**
 * How the product page translates a product the input port does not find: the status it answers
 * with and the not-found page it renders. The input port is stubbed to find nothing.
 */
@SpringBootTest(classes = EcommerceSampleApplication.class)
@AutoConfigureMockMvc
@DisplayName("Product page of a product not found")
class ProductPageNotFoundStatusIntegrationTest {

  private static final Pattern HEADING =
      Pattern.compile("class=\"error-page__title\">([^<]*)</h2>");

  @Autowired private MockMvc mockMvc;

  @Test
  @DisplayName("A malformed product address answers 404")
  void malformedProductAddressAnswersNotFound() throws Exception {
    final MockHttpServletResponse response =
        mockMvc.perform(get("/products/no-such-product")).andReturn().getResponse();

    assertThat(response.getStatus()).isEqualTo(404);
    assertThat(heading(response.getContentAsString())).isEqualTo("Page Not Found");
  }

  private static String heading(final String html) {
    final Matcher matcher = HEADING.matcher(html);
    assertThat(matcher.find()).as("the not-found page's heading").isTrue();
    return HtmlUtils.htmlUnescape(matcher.group(1)).strip();
  }

  @TestConfiguration
  static class NothingFound {

    @Bean
    @Primary
    GetProductByIdInputPort findsNoProduct() {
      return query -> GetProductByIdResult.notFound();
    }
  }
}
