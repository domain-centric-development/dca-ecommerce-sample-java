package dev.domaincentric.sample.ecommerce.product.adapter.incoming.web;

import dev.domaincentric.sample.ecommerce.product.application.getproductselection.GetProductSelectionInputPort;
import dev.domaincentric.sample.ecommerce.product.application.getproductselection.GetProductSelectionQuery;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Provides the homepage's "Discover products" slider as the model attribute {@code productSlider}.
 *
 * <p>The homepage belongs to another context; it renders the attribute without referencing this
 * one. The selection is drawn only for the homepage request {@code GET /}: every other request
 * returns early and gets no attribute, so no other page or API call pays for the draw.
 */
@ControllerAdvice
public class ProductSliderControllerAdvice {

  private static final String HOME_PAGE = "/";

  private final GetProductSelectionInputPort getProductSelection;

  public ProductSliderControllerAdvice(final GetProductSelectionInputPort getProductSelection) {
    this.getProductSelection = getProductSelection;
  }

  /**
   * Adds the slider to the model of the homepage request.
   *
   * @param model the model of the current request
   * @param request the current request
   */
  @ModelAttribute
  public void addProductSlider(final Model model, final HttpServletRequest request) {
    if (!"GET".equals(request.getMethod()) || !HOME_PAGE.equals(request.getRequestURI())) {
      return;
    }
    model.addAttribute(
        "productSlider",
        ProductSliderViewModel.fromResult(
            getProductSelection.execute(new GetProductSelectionQuery())));
  }
}
