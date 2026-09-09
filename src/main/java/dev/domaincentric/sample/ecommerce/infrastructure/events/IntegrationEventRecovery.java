package dev.domaincentric.sample.ecommerce.infrastructure.events;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.modulith.events.EventPublication;
import org.springframework.modulith.events.IncompleteEventPublications;
import org.springframework.modulith.events.ResubmissionOptions;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Bounded retry over the existing per-listener registry; exhausted rows remain FAILED and
 * inspectable.
 */
@Component
@EnableScheduling
public class IntegrationEventRecovery {
  private final IncompleteEventPublications publications;
  private final int maxAttempts;
  private final Duration baseDelay;
  private final boolean scheduled;

  public IntegrationEventRecovery(
      IncompleteEventPublications publications,
      @Value("${dca.events.retry.max-attempts:5}") int maxAttempts,
      @Value("${dca.events.retry.base-delay:PT0.2S}") Duration baseDelay,
      @Value("${dca.events.retry.enabled:true}") boolean scheduled) {
    if (maxAttempts < 1 || baseDelay.isNegative())
      throw new IllegalArgumentException("Invalid retry policy");
    this.publications = publications;
    this.maxAttempts = maxAttempts;
    this.baseDelay = baseDelay;
    this.scheduled = scheduled;
  }

  @Scheduled(fixedDelayString = "${dca.events.retry.poll-delay:200}")
  public void scheduledRetry() {
    if (scheduled) retryDue();
  }

  public void retryDue() {
    Instant now = Instant.now();
    publications.resubmitIncompletePublications(
        ResubmissionOptions.defaults().withFilter(p -> eligible(p, now)));
  }

  public boolean eligible(EventPublication publication, Instant now) {
    if (publication.getStatus() != EventPublication.Status.FAILED
        || publication.getCompletionAttempts() >= maxAttempts) return false;
    Instant last = publication.getLastResubmissionDate();
    if (last == null) last = publication.getPublicationDate();
    long multiplier = 1L << Math.min(30, Math.max(0, publication.getCompletionAttempts() - 1));
    return !now.isBefore(last.plus(baseDelay.multipliedBy(multiplier)));
  }

  /**
   * One intentional retry of a failed listener; neither payload nor identity nor acknowledged rows
   * change.
   */
  public void replayFailed(UUID publicationId) {
    publications.resubmitIncompletePublications(
        ResubmissionOptions.defaults()
            .withFilter(
                p ->
                    p.getIdentifier().equals(publicationId)
                        && p.getStatus() == EventPublication.Status.FAILED));
  }
}
