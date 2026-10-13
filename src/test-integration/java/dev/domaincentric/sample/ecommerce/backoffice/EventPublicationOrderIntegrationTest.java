package dev.domaincentric.sample.ecommerce.backoffice;

import static org.assertj.core.api.Assertions.assertThat;

import dev.domaincentric.sample.ecommerce.backoffice.application.geteventpublications.EventPublicationSummary;
import dev.domaincentric.sample.ecommerce.backoffice.application.geteventpublications.GetEventPublicationsInputPort;
import dev.domaincentric.sample.ecommerce.backoffice.application.geteventpublications.GetEventPublicationsQuery;
import dev.domaincentric.sample.ecommerce.infrastructure.EcommerceSampleApplication;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * The event publication log read through its input port in the wired application, against the
 * publications the shop's start-up recorded.
 */
@SpringBootTest(classes = EcommerceSampleApplication.class)
@DisplayName("Event publication log order")
class EventPublicationOrderIntegrationTest {

  @Autowired private GetEventPublicationsInputPort getEventPublications;

  @Test
  @DisplayName("Newest entry comes first")
  void entriesAreListedNewestPublicationFirst() {
    final List<Instant> published =
        getEventPublications.execute(new GetEventPublicationsQuery()).entries().stream()
            .map(EventPublicationSummary::publicationDate)
            .toList();

    assertThat(published).hasSizeGreaterThan(1);
    assertThat(published).isSortedAccordingTo(Comparator.reverseOrder());
  }
}
