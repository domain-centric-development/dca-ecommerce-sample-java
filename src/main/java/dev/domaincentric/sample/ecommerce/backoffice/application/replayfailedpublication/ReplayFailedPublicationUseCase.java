package dev.domaincentric.sample.ecommerce.backoffice.application.replayfailedpublication;

import org.springframework.stereotype.Service;

@Service
public class ReplayFailedPublicationUseCase implements ReplayFailedPublicationInputPort {
  private final EventPublicationRecoveryPort recovery;

  public ReplayFailedPublicationUseCase(EventPublicationRecoveryPort recovery) {
    this.recovery = recovery;
  }

  @Override
  public ReplayFailedPublicationResult execute(ReplayFailedPublicationCommand command) {
    recovery.replayFailed(command.publicationId());
    return new ReplayFailedPublicationResult();
  }
}
