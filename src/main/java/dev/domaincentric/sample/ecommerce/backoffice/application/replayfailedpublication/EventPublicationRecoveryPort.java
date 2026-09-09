package dev.domaincentric.sample.ecommerce.backoffice.application.replayfailedpublication;

import dev.domaincentric.dca.buildingblocks.hexagonal.port.out.OutputPort;

public interface EventPublicationRecoveryPort extends OutputPort {
  void replayFailed(java.util.UUID publicationId);
}
