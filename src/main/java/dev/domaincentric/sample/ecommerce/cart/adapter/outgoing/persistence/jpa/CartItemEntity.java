package dev.domaincentric.sample.ecommerce.cart.adapter.outgoing.persistence.jpa;

import jakarta.persistence.*;

@Entity
@Table(name = "cart_items")
public class CartItemEntity {

  @Id
  @Column(length = 64)
  private String id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "cart_id", nullable = false)
  private CartEntity cart;

  @Column(name = "product_id", nullable = false, length = 64)
  private String productId;

  @Column(nullable = false)
  private int quantity;

  @Column(name = "price_amount", nullable = false, precision = 19, scale = 2)
  private java.math.BigDecimal priceAmount;

  @Column(name = "price_currency", nullable = false, length = 3)
  private String priceCurrency;

  @Column(name = "position_units", nullable = false, length = 16000)
  private String positionUnits;

  public String getPositionUnits() {
    return positionUnits;
  }

  public void setPositionUnits(String value) {
    positionUnits = value;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public CartEntity getCart() {
    return cart;
  }

  public void setCart(CartEntity cart) {
    this.cart = cart;
  }

  public String getProductId() {
    return productId;
  }

  public void setProductId(String productId) {
    this.productId = productId;
  }

  public int getQuantity() {
    return quantity;
  }

  public void setQuantity(int quantity) {
    this.quantity = quantity;
  }

  public java.math.BigDecimal getPriceAmount() {
    return priceAmount;
  }

  public void setPriceAmount(java.math.BigDecimal priceAmount) {
    this.priceAmount = priceAmount;
  }

  public String getPriceCurrency() {
    return priceCurrency;
  }

  public void setPriceCurrency(String priceCurrency) {
    this.priceCurrency = priceCurrency;
  }
}
