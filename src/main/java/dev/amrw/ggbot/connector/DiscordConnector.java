package dev.amrw.ggbot.connector;

import dev.amrw.ggbot.config.BotConfig;
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

    private final BotConfig botConfig;
    private final DiscordApi api;

    public DiscordConnector(final BotConfigReader botConfigReader, final DiscordApiBuilder discordApiBuilder) {
        log.info("Initialising connection with Discord");
        botConfig = botConfigReader.getBotConfig()
                .orElseThrow(() -> new IllegalStateException("Could not load bot config"));
        api = discordApiBuilder.setToken(botConfig.getAuthToken()).login().join();
    }
}
