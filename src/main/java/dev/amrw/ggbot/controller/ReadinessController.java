package dev.amrw.ggbot.controller;

import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * A controller that verifies the application's readiness.
 */
@Log4j2
@RestController
@RequestMapping("/api/ready")
public class ReadinessController {

    /** @return {@link HttpStatus#OK} if the application is ready */
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<HttpStatus> ready() {
        log.info("Checking the readiness status");
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
