package dev.domaincentric.sample.ecommerce.product.adapter.incoming.bootstrap;

import dev.domaincentric.sample.ecommerce.product.application.createproduct.CreateProductCommand;
import dev.domaincentric.sample.ecommerce.product.application.createproduct.CreateProductInputPort;
import java.math.BigDecimal;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Incoming adapter that seeds the catalog at start-up, for demonstration purposes.
 *
 * <p>The catalog sells what this architecture is made of: the books the guide grew out of,
 * modelling supplies for a design workshop, and hexagon merchandise.
 *
 * <p>Start-up is a driving protocol like HTTP or an event stream: this adapter drives the Product
 * Catalog through its {@link CreateProductInputPort} — the same input port the REST resource and
 * the Open Host Service use — to create products with their initial price and stock level. Pricing
 * and Inventory pick those figures up from the {@code ProductCreatedEvent}, each through its own
 * trigger contract — the seeder never calls them, and this adapter knows only the one context it
 * belongs to. That is why it lives inside the Product context rather than in {@code
 * infrastructure}: there it would need the context's application layer, which the module exposes to
 * nobody. The .NET twin's {@code SampleDataSeeder} has the same place and shape.
 *
 * <p>Runs as an {@link ApplicationRunner} and manages no transaction of its own: each {@code
 * CreateProduct} call runs inside the use case's transaction boundary, which is where a transaction
 * belongs (DCA-LAY-004). An incoming adapter drives, it does not decide how far a unit of work
 * reaches.
 */
@Component
public class SampleDataInitializer implements ApplicationRunner {

  private final CreateProductInputPort createProduct;

  public SampleDataInitializer(final CreateProductInputPort createProduct) {
    this.createProduct = createProduct;
  }

  @Override
  public void run(final ApplicationArguments args) {
    loadSampleProducts();
  }

  private void loadSampleProducts() {
    // Books — the sources this architecture was synthesized from
    createProduct(
        "BOOK-001",
        "Domain-Driven Design",
        "The seminal work by Eric Evans that introduced the software industry to Domain-Driven Design. This essential guide teaches you how to tackle complexity in the heart of software by connecting implementation to an evolving model of the business domain.",
        "/images/products/ddd-book.webp",
        54.99,
        "Books",
        20);

    createProduct(
        "BOOK-002",
        "Clean Architecture",
        "Robert C. Martin's definitive guide to software structure and design. Learn the universal rules of software architecture that dramatically improve developer productivity throughout the life of any software system.",
        "/images/products/clean-architecture-book.webp",
        39.99,
        "Books",
        35);

    createProduct(
        "BOOK-003",
        "Implementing Domain-Driven Design",
        "Vaughn Vernon's hands-on companion to Evans: how aggregates, domain events and bounded contexts actually get built. The chapters on aggregate design rules and event-driven integration are the backbone of every context in this shop.",
        "/images/products/iddd-book.webp",
        49.99,
        "Books",
        18);

    createProduct(
        "BOOK-004",
        "Domain-Driven Design Distilled",
        "The short version of Vernon's work — strategic design, context mapping and event storming in under 200 pages. The book to hand a colleague who has two hours rather than two months.",
        "/images/products/ddd-distilled-book.webp",
        24.99,
        "Books",
        42);

    createProduct(
        "BOOK-005",
        "Hexagonal Architecture Explained",
        "Alistair Cockburn, with Juan Manuel Garrido de Paz, finally wrote the book on the pattern he named in 2005. Ports, adapters and the configurable dependency straight from the source, with two decades of misreadings corrected.",
        "/images/products/hexagonal-architecture-book.webp",
        34.99,
        "Books",
        26);

    createProduct(
        "BOOK-006",
        "Learning Domain-Driven Design",
        "Vlad Khononov connects strategic design to architectural style: which subdomain deserves a rich domain model, and which one is perfectly well served by a transaction script. The reasoning behind pattern selection per subdomain.",
        "/images/products/learning-ddd-book.webp",
        44.99,
        "Books",
        22);

    createProduct(
        "BOOK-007",
        "Patterns of Enterprise Application Architecture",
        "Martin Fowler's catalogue of the patterns every layered system rediscovers sooner or later — Repository, Unit of Work, Data Mapper, Service Layer. Twenty years on, still the reference for what a name in your adapter layer actually promises.",
        "/images/products/poeaa-book.webp",
        59.99,
        "Books",
        14);

    createProduct(
        "BOOK-008",
        "Team Topologies",
        "Matthew Skelton and Manuel Pais on the other half of the boundary problem: a bounded context that no single team owns will not stay bounded. Stream-aligned teams, platform teams, and the inverse Conway manoeuvre.",
        "/images/products/team-topologies-book.webp",
        29.99,
        "Books",
        30);

    // Modeling — everything a design workshop runs on
    createProduct(
        "STICKY-001",
        "Event Storming Sticky Note Kit",
        "Everything a modelling workshop needs, in the canonical colours: orange for domain events, blue for commands, yellow for aggregates, lilac for policies, pink for external systems. 500 sheets with extra-strong adhesive, plus a printed legend so nobody has to ask what the pink ones mean.",
        "/images/products/event-storming-stickies.webp",
        34.99,
        "Modeling",
        40);

    createProduct(
        "MAGNET-001",
        "Hexagon Whiteboard Magnets, Set of 24",
        "Twenty-four laser-cut hexagon magnets in six colours, dry-erase on the face. Rearrange your bounded contexts until the context map stops looking like a plate of spaghetti — then wipe them clean and do it again tomorrow.",
        "/images/products/hexagon-magnets.webp",
        24.99,
        "Modeling",
        35);

    createProduct(
        "POSTER-001",
        "Context Map Poster, A1",
        "All the strategic relationship patterns on one wall: Shared Kernel, Customer/Supplier, Conformist, Anti-Corruption Layer, Open Host Service, Published Language, Separate Ways, Partnership — and the Big Ball of Mud, so everyone can see where they are. Matte 200 g/m², ships rolled in a tube.",
        "/images/products/context-map-poster.webp",
        19.99,
        "Modeling",
        60);

    createProduct(
        "CARDS-001",
        "DDD Pattern Card Deck",
        "Fifty-four cards, one pattern each: intent on the front, forces and traps on the back. Deal them out at the start of a design session so the team argues with the card instead of with each other.",
        "/images/products/pattern-card-deck.webp",
        22.99,
        "Modeling",
        45);

    // Apparel
    createProduct(
        "SHIRT-001",
        "\"Ports & Adapters\" T-Shirt",
        "Heavyweight organic cotton with a screen-printed hexagon: incoming port on one edge, outgoing port on the other, domain in the middle. Explains your architecture before you have opened your laptop.",
        "/images/products/ports-adapters-tshirt.webp",
        29.99,
        "Apparel",
        80);

    createProduct(
        "HOODIE-001",
        "\"Domain over Framework\" Hoodie",
        "Brushed-fleece hoodie in midnight navy, the slogan across the chest and the dependency arrow pointing inward on the sleeve. Warm enough for a data centre, quiet enough for a customer workshop.",
        "/images/products/domain-hoodie.webp",
        59.99,
        "Apparel",
        40);

    createProduct(
        "CAP-001",
        "Hexagon Embroidered Cap",
        "Six-panel cotton twill with a golden hexagon embroidered on the front. Structured crown, curved brim, adjustable strap — the pattern on your head instead of on your slides.",
        "/images/products/hexagon-cap.webp",
        24.99,
        "Apparel",
        50);

    // Desk & Office
    createProduct(
        "MUG-001",
        "\"Ubiquitous Language\" Mug",
        "350 ml of stoneware making a single point: one term, one meaning, everyone at the table. Dishwasher-safe and insulated well enough to survive a two-hour glossary discussion.",
        "/images/products/ubiquitous-language-mug.webp",
        16.99,
        "Desk & Office",
        90);

    createProduct(
        "COASTER-001",
        "Hexagon Wooden Coasters, Set of 6",
        "Six oiled-oak hexagons, laser-engraved with concentric boundaries. The aggregates keep their invariants and your desk keeps its finish.",
        "/images/products/hexagon-coasters.webp",
        27.99,
        "Desk & Office",
        30);

    createProduct(
        "NOTEBOOK-001",
        "Hex-Grid Modeling Notebook",
        "A5 hardcover with 192 pages of hexagonal grid instead of squares, so contexts, aggregates and their neighbours almost sketch themselves. Lay-flat binding, ribbon marker, elastic band.",
        "/images/products/hex-notebook.webp",
        18.99,
        "Desk & Office",
        65);

    createProduct(
        "HEXAGON-001",
        "Wooden Hexagon Desk Model",
        "A solid beech hexagon on a walnut base, 12 cm across, with the domain engraved at the centre and three port notches cut into the edges. The hexagon you can actually buy — hand-finished in small batches, which is why there are never many in stock.",
        "/images/products/wooden-hexagon.webp",
        39.99,
        "Desk & Office",
        8);

    // Stickers & Pins
    createProduct(
        "STICKER-001",
        "Hexagon Sticker Sheet",
        "Six die-cut vinyl hexagons, weatherproof and residue-free: ports, adapters, aggregate, domain event, context boundary, and one left blank for the purists. Laptop lid, water bottle, or the frame of the whiteboard.",
        "/images/products/hexagon-stickers.webp",
        9.99,
        "Stickers & Pins",
        150);

    createProduct(
        "PIN-001",
        "\"Bounded Context\" Enamel Pin",
        "Hard-enamel pin, 25 mm, a gold-plated boundary around a deep magenta context with three ports on its edge. Butterfly clutch, backing card with a one-paragraph definition for the colleague who asks.",
        "/images/products/bounded-context-pin.webp",
        12.99,
        "Stickers & Pins",
        75);

    System.out.println(
        "Sample data initialized: 21 products; Pricing and Inventory follow via"
            + " ProductCreatedEvent");
  }

  private void createProduct(
      final String sku,
      final String name,
      final String description,
      final String imageUrl,
      final double price,
      final String category,
      final int initialStock) {

    createProduct.execute(
        new CreateProductCommand(
            sku,
            name,
            description,
            imageUrl,
            BigDecimal.valueOf(price),
            "EUR",
            category,
            initialStock));
  }
}
