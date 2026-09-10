package dev.domaincentric.sample.ecommerce.cart.adapter.outgoing.persistence.jpa;

import dev.domaincentric.sample.ecommerce.cart.application.shared.ShoppingCartRepository;
import dev.domaincentric.sample.ecommerce.cart.domain.model.*;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Money;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.PageResult;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.PagingRequest;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Price;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.ProductId;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.specification.CompositeSpecification;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Primary
public class JpaShoppingCartRepository implements ShoppingCartRepository {

  private final CartJpaRepository cartRepo;
  private final CartSpecToJpa specTranslator;

  public JpaShoppingCartRepository(
      final CartJpaRepository cartRepo, final CartSpecToJpa specTranslator) {
    this.cartRepo = cartRepo;
    this.specTranslator = specTranslator;
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<ShoppingCart> findById(final CartId id) {
    return cartRepo.findById(id.value()).map(this::toDomain);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<ShoppingCart> findByIdForCustomer(
      final CartId cartId, final CustomerId customerId) {
    return findById(cartId).filter(cart -> cart.customerId().equals(customerId));
  }

  @Override
  @Transactional(readOnly = true)
  public List<ShoppingCart> findByCustomerId(final CustomerId customerId) {
    return cartRepo.findByCustomerId(customerId.value()).stream().map(this::toDomain).toList();
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<ShoppingCart> findActiveCartByCustomerId(final CustomerId customerId) {
    return cartRepo
        .findFirstByCustomerIdAndStatusOrderByUpdatedAtDesc(
            customerId.value(), CartStatus.ACTIVE.name())
        .map(this::toDomain);
  }

  @Override
  @Transactional(readOnly = true)
  public List<ShoppingCart> findAll() {
    return cartRepo.findAll().stream().map(this::toDomain).toList();
  }

  @Override
  @Transactional(readOnly = true)
  public PageResult<ShoppingCart> findBy(
      final CompositeSpecification<ShoppingCart> specification, final PagingRequest pageQuery) {
    final CartSpecToJpa translator = requireTranslator();
    final Specification<CartEntity> jpaSpec = specification.accept(translator);
    final var pageable = PageRequest.of(pageQuery.pageNumber(), pageQuery.pageSize());
    final var page = cartRepo.findAll(jpaSpec, pageable).map(this::toDomain);
    return new PageResult<>(
        page.getContent(), page.getTotalElements(), pageQuery.pageNumber(), pageQuery.pageSize());
  }

  @Override
  @Transactional
  public ShoppingCart save(final ShoppingCart cart) {
    final CartEntity entity = toEntity(cart);
    entity.setUpdatedAt(Instant.now());
    final CartEntity saved = cartRepo.saveAndFlush(entity);
    return toDomain(saved);
  }

  @Override
  @Transactional
  public void deleteById(final CartId id) {
    cartRepo.deleteById(id.value());
  }

  private CartSpecToJpa requireTranslator() {
    if (this.specTranslator == null) {
      throw new IllegalStateException("CartSpecToJpa translator not configured");
    }
    return this.specTranslator;
  }

  private ShoppingCart toDomain(final CartEntity entity) {
    final List<ShoppingCart.StoredItem> storedItems = new ArrayList<>();
    if (entity.getItems() != null) {
      for (final CartItemEntity it : entity.getItems()) {
        storedItems.add(
            new ShoppingCart.StoredItem(
                CartItemId.of(it.getId()),
                ProductId.of(it.getProductId()),
                Quantity.of(it.getQuantity()),
                Price.of(
                    Money.of(
                        it.getPriceAmount(),
                        java.util.Currency.getInstance(it.getPriceCurrency()))),
                it.getPositionUnits()));
      }
    }

    return ShoppingCart.reconstitute(
        CartId.of(entity.getId()),
        CustomerId.of(entity.getCustomerId()),
        CartStatus.valueOf(entity.getStatus()),
        storedItems);
  }

  private CartEntity toEntity(final ShoppingCart cart) {
    final CartEntity e = new CartEntity();
    e.setId(cart.id().value());
    e.setCustomerId(cart.customerId().value());
    e.setStatus(cart.status().name());

    final List<CartItemEntity> itemEntities = new ArrayList<>();
    for (final CartItem item : cart.items()) {
      final CartItemEntity ie = new CartItemEntity();
      ie.setId(item.id().value());
      ie.setCart(e);
      ie.setProductId(item.productId().value());
      ie.setQuantity(item.quantity().value());
      ie.setPriceAmount(item.priceAtAddition().value().amount());
      ie.setPriceCurrency(item.priceAtAddition().value().currency().getCurrencyCode());
      ie.setPositionUnits(item.storedUnits());
      itemEntities.add(ie);
    }
    e.setItems(itemEntities);
    return e;
  }
}
