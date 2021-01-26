package dev.amrw.ggbot.connector;

import dev.amrw.ggbot.config.BotConfigReader;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;

/**
 * Point of communication with Discord servers.
 */
@Getter
@Log4j2
public class DiscordConnector {

    private final DiscordApi api;

    public DiscordConnector(final DiscordApiBuilder discordApiBuilder, final BotConfigReader botConfigReader) {
        log.info("Initialising connection with Discord");
        this.api = discordApiBuilder.setToken(botConfigReader.getAuthToken()).login().join();
    }
}
