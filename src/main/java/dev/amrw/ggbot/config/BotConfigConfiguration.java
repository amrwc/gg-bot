package dev.amrw.ggbot.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import dev.amrw.ggbot.resource.BotConfig;
import dev.amrw.ggbot.resource.ResourceReader;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.autoconfigure.condition.ConditionalOnResource;
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
@ConditionalOnResource(resources = {BotConfig.PATH})
public class BotConfigConfiguration {

    @Bean
    public BotConfig botConfig() {
        log.info("Reading {} config", BotConfig.PATH);
        final var configReader = new ResourceReader(new ObjectMapper(new YAMLFactory()));
        return configReader.readResource(BotConfig.PATH, BotConfig.class)
                .orElseThrow(() -> new IllegalStateException("Failed to read " + BotConfig.PATH));
    }
}
