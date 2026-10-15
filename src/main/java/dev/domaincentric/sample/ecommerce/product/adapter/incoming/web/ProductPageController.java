package dev.domaincentric.sample.ecommerce.product.adapter.incoming.web;

import dev.domaincentric.sample.ecommerce.product.application.getallproducts.GetAllProductsInputPort;
import dev.domaincentric.sample.ecommerce.product.application.getallproducts.GetAllProductsQuery;
import dev.domaincentric.sample.ecommerce.product.application.getallproducts.GetAllProductsResult;
import dev.domaincentric.sample.ecommerce.product.application.getproductbyid.GetProductByIdInputPort;
import dev.domaincentric.sample.ecommerce.product.application.getproductbyid.GetProductByIdQuery;
import dev.domaincentric.sample.ecommerce.product.application.getproductbyid.GetProductByIdResult;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * MVC Controller for rendering product pages using Pug templates.
 *
 * <p>This controller handles traditional server-side rendered pages using Pug4j templates. Unlike
 * REST API controllers which return JSON, this controller returns HTML views.
 *
 * <p><b>Clean Architecture:</b> This controller depends on use case interfaces (input ports)
 * instead of application services, following the Dependency Inversion Principle.
 *
 * <p><b>ViewModel Pattern:</b> Use Case → Result → Controller → ViewModel → Template
 *
 * <ul>
 *   <li>Use cases return domain read models wrapped in Result objects
 *   <li>Controller converts Results to page-specific ViewModels
 *   <li>ViewModels use primitives only (String, BigDecimal, int, boolean)
 * </ul>
 *
 * <p><b>Naming Convention:</b> MVC controllers use {@code @Controller} annotation and end with
 * "Controller" suffix. REST controllers use {@code @RestController} and end with "Resource" suffix.
 *
 * <p><b>Template Location:</b> {@code src/main/resources/templates/product/}
 *
 * <p><b>Template Engine:</b> Pug4j (Java implementation of Pug/Jade)
 */
@Controller
@RequestMapping("/products")
public class ProductPageController {

  private final GetAllProductsInputPort getAllProductsUseCase;
  private final GetProductByIdInputPort getProductByIdUseCase;

  public ProductPageController(
      final GetAllProductsInputPort getAllProductsUseCase,
      final GetProductByIdInputPort getProductByIdUseCase) {
    this.getAllProductsUseCase = getAllProductsUseCase;
    this.getProductByIdUseCase = getProductByIdUseCase;
  }

  /**
   * Displays the product catalog page.
   *
   * <p>Returns a list of all products rendered using the Pug template. The use case result is
   * converted to a page-specific ViewModel.
   *
   * @param model Spring MVC model to pass data to the view
   * @return view name "product/catalog" which resolves to templates/product/catalog.pug
   */
  @GetMapping
  public String showProductCatalog(final Model model) {
    final GetAllProductsResult result = getAllProductsUseCase.execute(new GetAllProductsQuery());

    // Convert Result → page-specific ViewModel
    final ProductCatalogPageViewModel viewModel = ProductCatalogPageViewModel.fromResult(result);

    model.addAttribute("productCatalog", viewModel);
    model.addAttribute("title", viewModel.pageTitle());

    return "product/catalog";
  }

  /**
   * Displays a single product detail page.
   *
   * <p>Shows detailed information about a specific product including availability information from
   * the Inventory context. The use case result is converted to a page-specific ViewModel.
   *
   * @param id the product ID
   * @param model Spring MVC model to pass data to the view
   * @param response the response, answered with status 404 when no product has the ID
   * @return view name "product/detail", or "error/404" with status 404 if product not found
   */
  @GetMapping("/{id}")
  public String showProductDetail(
      @PathVariable final String id, final Model model, final HttpServletResponse response) {
    final GetProductByIdResult result = getProductByIdUseCase.execute(new GetProductByIdQuery(id));

    if (!result.found()) {
      response.setStatus(HttpServletResponse.SC_NOT_FOUND);
      return "error/404";
    }

    // Convert Result → page-specific ViewModel
    final ProductDetailPageViewModel viewModel = ProductDetailPageViewModel.fromResult(result);

    model.addAttribute("productDetail", viewModel);
    model.addAttribute("title", viewModel.pageTitle());

    return "product/detail";
  }

  /**
   * Displays a sample landing page demonstrating Pug4j features.
   *
   * @param model Spring MVC model
   * @return view name "product/sample"
   */
  @GetMapping("/sample")
  public String showSamplePage(final Model model) {
    model.addAttribute("title", "Pug4j Sample Page");
    model.addAttribute("message", "Welcome to the Product Catalog");
    model.addAttribute("features", List.of("DDD Architecture", "Clean Code", "Pug Templates"));

    return "product/sample";
  }
}
