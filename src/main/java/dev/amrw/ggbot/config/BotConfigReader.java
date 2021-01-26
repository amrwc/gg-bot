package dev.amrw.ggbot.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;

/**
 * Config reader for the bot properties.
 */
@Log4j2
public class BotConfigReader {

    static final String BOT_CONFIG_PATH = "/bot-config.yml";

    private final ObjectMapper objectMapper;

    public BotConfigReader(final ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /** @return bot authentication token */
    public String getAuthToken() {
        try {
            var configFile = BotConfigReader.class.getResourceAsStream(BOT_CONFIG_PATH);
            return objectMapper.readValue(configFile, BotConfig.class).getAuthToken();
        } catch (final IOException exception) {
            log.error("Failed to read value from {}", BOT_CONFIG_PATH);
            return "";
        }
    }
}
