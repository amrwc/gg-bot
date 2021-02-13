package dev.amrw.ggbot.config;

import dev.amrw.ggbot.repository.ConfigRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
                configRepository.findString(EMBED_COLOUR),
                configRepository.findString(TRIGGER)
        );
    }
}
