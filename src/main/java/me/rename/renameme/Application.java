package me.rename.renameme;

import lombok.extern.log4j.Log4j2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * The application's entry point.
 */
@Log4j2
@SpringBootApplication
public class Application {

    static final String WELCOME_MESSAGE = "Welcome! Read the readme to get started.";

    public static void main(final String[] args) {
        SpringApplication.run(Application.class, args);
    }

    public static String getWelcomeMessage() {
        log.info(WELCOME_MESSAGE);
        return WELCOME_MESSAGE;
    }
}
