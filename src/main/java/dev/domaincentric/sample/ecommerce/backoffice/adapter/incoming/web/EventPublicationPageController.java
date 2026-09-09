package dev.domaincentric.sample.ecommerce.backoffice.adapter.incoming.web;

import dev.domaincentric.sample.ecommerce.backoffice.application.geteventpublications.GetEventPublicationsInputPort;
import dev.domaincentric.sample.ecommerce.backoffice.application.geteventpublications.GetEventPublicationsQuery;
import dev.domaincentric.sample.ecommerce.backoffice.application.geteventpublications.GetEventPublicationsResult;
import dev.domaincentric.sample.ecommerce.backoffice.application.replayfailedpublication.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * MVC Controller for the backoffice pages.
 *
 * <p>Handles the backoffice login page and the event publication log page. The login page is served
 * separately from the storefront login (which uses JWT) — the backoffice uses form-based session
 * authentication.
 *
 * <p><b>ViewModel Pattern:</b> Use Case -> Result -> Controller -> ViewModel -> Template
 */
@Controller
@RequestMapping("/backoffice")
public class EventPublicationPageController {

  private final GetEventPublicationsInputPort getEventPublicationsInputPort;
  private final ReplayFailedPublicationInputPort replay;

  public EventPublicationPageController(
      final GetEventPublicationsInputPort getEventPublicationsInputPort,
      ReplayFailedPublicationInputPort replay) {
    this.replay = replay;
    this.getEventPublicationsInputPort = getEventPublicationsInputPort;
  }

  /**
   * Displays the backoffice login page.
   *
   * @param error whether the previous login attempt failed
   * @param logout whether the user just logged out
   * @param model the Spring MVC model
   * @return the Pug template name
   */
  @GetMapping("/login")
  public String showLoginPage(
      @RequestParam(required = false) final String error,
      @RequestParam(required = false) final String logout,
      final Model model) {
    model.addAttribute("error", error != null);
    model.addAttribute("logout", logout != null);
    model.addAttribute("title", "Backoffice Login");

    return "backoffice/login";
  }

  /**
   * Displays the event publication log page.
   *
   * @param model the Spring MVC model
   * @return the Pug template name
   */
  @GetMapping("/events")
  public String showEventPublicationLog(final Model model) {
    final GetEventPublicationsResult result =
        getEventPublicationsInputPort.execute(new GetEventPublicationsQuery());

    final EventPublicationPageViewModel viewModel = EventPublicationPageViewModel.from(result);

    model.addAttribute("eventLog", viewModel);
    model.addAttribute("title", "Event Publication Log");

    return "backoffice/events";
  }

  @PostMapping("/events/{id}/replay")
  public String replayFailed(@PathVariable java.util.UUID id) {
    replay.execute(new ReplayFailedPublicationCommand(id));
    return "redirect:/backoffice/events";
  }
}
