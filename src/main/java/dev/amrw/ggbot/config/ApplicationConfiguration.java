package dev.amrw.ggbot.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import dev.amrw.ggbot.connector.DiscordConnector;
import dev.amrw.ggbot.listener.PingPongListener;
import org.javacord.api.DiscordApiBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnResource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * The application's bean factory.
 */
@Configuration
public class ApplicationConfiguration {

    /**
     * This bean should have been created <em>only if</em> the bot config resource is present.
     * @return {@link DiscordConnector} instance
     */
    @Bean
    @ConditionalOnResource(resources = BotConfig.PATH)
    public DiscordConnector discordConnector() {
        final var objectMapper = new ObjectMapper(new YAMLFactory());
        final var discordConnector = new DiscordConnector(new ConfigReader(objectMapper), new DiscordApiBuilder());
        discordConnector.getApi().addListener(new PingPongListener());
        return discordConnector;
    }
}
