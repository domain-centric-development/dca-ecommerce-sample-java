package dev.domaincentric.sample.ecommerce.backoffice.adapter.outgoing.persistence;

import dev.domaincentric.sample.ecommerce.backoffice.application.replayfailedpublication.EventPublicationRecoveryPort;
import dev.domaincentric.sample.ecommerce.infrastructure.events.IntegrationEventRecovery;
import org.springframework.stereotype.Component;

@Component
public class RegistryEventPublicationRecoveryAdapter implements EventPublicationRecoveryPort {
  private final IntegrationEventRecovery recovery;

  public RegistryEventPublicationRecoveryAdapter(IntegrationEventRecovery recovery) {
    this.recovery = recovery;
  }

  @Override
  public void replayFailed(java.util.UUID publicationId) {
    recovery.replayFailed(publicationId);
  }
}
