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

    /**
     * NOTE: It <em>requires</em> a valid authentication token, which should come from the given {@link BotConfig}.
     * Otherwise, if the token is invalid or not present, the <code>.login().join()</code> part will fail, and the bean
     * will not get produced. Therefore, it's important to have this step done <em>after</em> {@link BotConfig} bean
     * could have been produced.
     * @param botConfig {@link BotConfig} produced from a config file
     * @return {@link DiscordApi} bean
     */
    @Bean
    public DiscordApi discordApi(final BotConfig botConfig) {
        log.info("Initialising connection with Discord");
        final var apiBuilder = new DiscordApiBuilder().setToken(botConfig.getAuthToken());
        listeners.forEach(apiBuilder::addListener);
        return apiBuilder.login().join();
    }
}
