package dev.amrw.bin.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import dev.amrw.bin.config.Config;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.util.Optional;

/**
 * Utility for reading the config file(s).
 */
@Log4j2
public class ConfigReader {

    private static final String DEFAULT_CONFIG_PATH = "/config.yml";

    private final ObjectMapper mapper;

    public ConfigReader() {
        mapper = new ObjectMapper(new YAMLFactory());
    }

    /** @return default {@link Config} */
    public Optional<Config> getDefaultConfig() {
        try (final var inputStream = ConfigReader.class.getResourceAsStream(DEFAULT_CONFIG_PATH)) {
            return Optional.ofNullable(mapper.readValue(inputStream, Config.class));
        } catch (final IOException exception) {
            log.error("Error getting default config '{}'", DEFAULT_CONFIG_PATH, exception);
            return Optional.empty();
        }
    }
}
