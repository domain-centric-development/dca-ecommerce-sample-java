package dev.domaincentric.sample.ecommerce.backoffice.application.replayfailedpublication;

import dev.domaincentric.dca.buildingblocks.hexagonal.port.in.UseCase;

public interface ReplayFailedPublicationInputPort
    extends UseCase<ReplayFailedPublicationCommand, ReplayFailedPublicationResult> {}
