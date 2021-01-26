package dev.amrw.ggbot.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

/**
 * Config reader for the bot properties.
 */
@Log4j2
public class BotConfigReader {

    public static final String BOT_CONFIG_PATH = "/bot-config.yml";

    private final ObjectMapper objectMapper;

    public BotConfigReader(final ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /** @return {@link BotConfig} instance built from {@link #BOT_CONFIG_PATH} */
    public Optional<BotConfig> getBotConfig() {
        try {
            return Optional.of(objectMapper.readValue(readBotConfig(), BotConfig.class));
        } catch (final IOException exception) {
            log.error("Failed to read values from {}", BOT_CONFIG_PATH, exception);
            return Optional.empty();
        }
    }

    protected InputStream readBotConfig() {
        return BotConfigReader.class.getResourceAsStream(BOT_CONFIG_PATH);
    }
}
