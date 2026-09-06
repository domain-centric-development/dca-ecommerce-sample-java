package dev.domaincentric.sample.ecommerce.checkout.application.getpaymentproviders;

/**
 * Query model for retrieving available payment providers.
 *
 * <p>This is a marker record since no input parameters are required to retrieve payment providers.
 */
public record GetPaymentProvidersQuery() {

  /**
   * Creates a new query instance.
   *
   * @return a new GetPaymentProvidersQuery
   */
  public static GetPaymentProvidersQuery create() {
    return new GetPaymentProvidersQuery();
  }
}
