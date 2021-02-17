package dev.amrw.ggbot.config;

import dev.amrw.ggbot.repository.ConfigRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.awt.Color;

/**
 * Bean factory for {@link BotConfig}.
 * <p>
 * NOTE: It's in its own {@link Configuration} to allow explicitly relying on its (non)existence using the
 * <code>@AutoConfigureAfter</code> and <code>@ConditionalOnMissingBean/@ConditionalOnBean</code> annotation pair.
 * @see org.springframework.boot.autoconfigure.AutoConfigureAfter
 * @see org.springframework.boot.autoconfigure.condition.ConditionalOnBean
 * @see org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
 */
@Log4j2
@Configuration
public class BotConfigConfiguration {

    private static final String DISCORD_AUTH_TOKEN = "DISCORD_AUTH_TOKEN";
    private static final String EMBED_COLOUR = "EMBED_COLOUR";
    private static final Color DEFAULT_EMBED_COLOUR = Color.ORANGE;
    private static final String TRIGGER = "TRIGGER";

    private final ConfigRepository configRepository;

    public BotConfigConfiguration(final ConfigRepository configRepository) {
        this.configRepository = configRepository;
    }

    @Bean
    public BotConfig botConfig() {
        log.info("Building BotConfig");
        return new BotConfig(
                configRepository.findString(DISCORD_AUTH_TOKEN),
                parseColour(configRepository.findString(EMBED_COLOUR)),
                configRepository.findString(TRIGGER)
        );
    }

    /**
     * Parses colour name into {@link Color} using its field name via reflection. Defaults to
     * {@link #DEFAULT_EMBED_COLOUR} in case of an exception.
     * <p>
     * Read more on the solution and reasons for it <a href="https://stackoverflow.com/a/3772327/10620237">here</a>.
     * @param colour field name from the {@link Color} class.
     * @return parsed {@link Color} or {@link #DEFAULT_EMBED_COLOUR} in case an exception has been raised
     */
    private Color parseColour(final String colour) {
        try {
            final var field = Color.class.getField(colour);
            return (Color) field.get(null);
        } catch (final Exception exception) {
            log.error("Error when parsing colour: {}", colour, exception);
            return DEFAULT_EMBED_COLOUR;
        }
    }
}
