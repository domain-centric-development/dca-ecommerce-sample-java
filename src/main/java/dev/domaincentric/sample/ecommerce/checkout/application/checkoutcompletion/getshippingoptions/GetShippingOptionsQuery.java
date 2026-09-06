package dev.domaincentric.sample.ecommerce.checkout.application.getshippingoptions;

/**
 * Query model for retrieving available shipping options.
 *
 * <p>This is a marker record since no input parameters are required to retrieve shipping options.
 */
public record GetShippingOptionsQuery() {

  /**
   * Creates a new query instance.
   *
   * @return a new GetShippingOptionsQuery
   */
  public static GetShippingOptionsQuery create() {
    return new GetShippingOptionsQuery();
  }
}
