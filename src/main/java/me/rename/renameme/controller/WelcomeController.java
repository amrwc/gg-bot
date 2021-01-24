package me.rename.renameme.controller;

import lombok.extern.log4j.Log4j2;
import me.rename.renameme.model.WelcomeMessage;
import me.rename.renameme.request.WelcomeMessageRequest;
import me.rename.renameme.service.WelcomeMessageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Optional;

/**
 * A controller that welcomes the user.
 */
@Log4j2
@RestController
@RequestMapping("/api/welcome")
public class WelcomeController {

    private final WelcomeMessageService service;

    public WelcomeController(final WelcomeMessageService service) {
        this.service = service;
    }

    /**
     * Fetches the welcome message with the given ID.
     * @param id optional ID
     * @return welcome message with the given ID
     */
    @RequestMapping(path = "{id}", method = RequestMethod.GET)
    public ResponseEntity<WelcomeMessage> welcome(@PathVariable Optional<Long> id) {
        log.info("Fetching the welcome message with id {}", id);
        return service.findWelcomeMessageById(id.orElse(1L))
                .map(welcomeMessage -> new ResponseEntity<>(welcomeMessage, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * Persists the given welcome message in the database.
     * @param request welcome message to persist
     * @return HTTP status
     */
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<HttpStatus> addWelcome(@RequestBody @Valid WelcomeMessageRequest request) {
        log.info("Creating a welcome message: {}", request);
        service.createWelcomeMessage(request.getContent());
        return new ResponseEntity<>(HttpStatus.CREATED);
    }
}
