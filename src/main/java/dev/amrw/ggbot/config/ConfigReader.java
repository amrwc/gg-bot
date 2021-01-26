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
public class ConfigReader {

    private final ObjectMapper objectMapper;

    public ConfigReader(final ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Reads values from the given resource into the given config POJO.
     * @param resourcePath path to the config resource
     * @param configClass POJO that matches the fields of the given file
     * @param <T> the given type
     * @return data from the given resource, or an empty {@link Optional} in case of an {@link IOException}
     */
    public <T> Optional<T> getConfig(final String resourcePath, final Class<T> configClass) {
        try {
            return Optional.of(objectMapper.readValue(getResource(resourcePath), configClass));
        } catch (final IOException exception) {
            log.error("Failed to read values from {}", resourcePath, exception);
            return Optional.empty();
        }
    }

    protected InputStream getResource(final String path) {
        return ConfigReader.class.getResourceAsStream(path);
    }
}
