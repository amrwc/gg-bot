package dev.amrw.ggbot.config;

import dev.amrw.ggbot.resource.BotConfig;
import lombok.extern.log4j.Log4j2;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.listener.GloballyAttachableListener;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Set;

/**
 * Bean factory for {@link DiscordApi}.
 */
@Log4j2
@Configuration
@ConditionalOnBean(BotConfig.class)
@AutoConfigureAfter(BotConfigConfiguration.class)
public class DiscordApiConfiguration {

    private final Set<GloballyAttachableListener> listeners;

    public DiscordApiConfiguration(final Set<GloballyAttachableListener> listeners) {
        this.listeners = listeners;
    }

    @Bean
    public DiscordApi discordApi(final BotConfig botConfig) {
        log.info("Initialising connection with Discord");
        final var api = new DiscordApiBuilder().setToken(botConfig.getAuthToken()).login().join();
        listeners.forEach(api::addListener);
        return api;
    }
}
