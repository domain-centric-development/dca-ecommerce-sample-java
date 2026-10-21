package dev.domaincentric.sample.ecommerce.account.adapter.incoming.web;

import org.jspecify.annotations.Nullable;

/**
 * The page a link asks the login or the registration to return to afterwards.
 *
 * <p>A link anyone can build hands it over, so the account pages follow it, and carry it on to each
 * other, only when it is a page of the shop.
 */
final class ReturnPage {

  private ReturnPage() {}

  /**
   * The link to another account page that carries the return page on, when it is a page of the
   * shop.
   *
   * @param path the account page the link leads to, e.g. {@code /login}
   * @param returnUrl the return page as the request carried it, or {@code null} without one
   * @return the path with the return page, or the bare path
   */
  static String carriedOnTo(final String path, final @Nullable String returnUrl) {
    return isShopPage(returnUrl) ? path + "?returnUrl=" + returnUrl : path;
  }

  /**
   * Whether a return page is a page of the shop: a path of its own, never another site's address
   * nor one a browser reads as such ({@code //host}, {@code /\host}).
   *
   * @param returnUrl the return page as the request carried it, or {@code null} without one
   * @return {@code true} only for a path of the shop
   */
  static boolean isShopPage(final @Nullable String returnUrl) {
    return returnUrl != null
        && returnUrl.startsWith("/")
        && !returnUrl.startsWith("//")
        && !returnUrl.startsWith("/\\");
  }
}
