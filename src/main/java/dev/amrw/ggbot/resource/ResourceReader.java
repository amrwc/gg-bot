package dev.amrw.ggbot.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

/**
 * Reader for resource files.
 */
@Log4j2
public class ResourceReader {

    private final ObjectMapper objectMapper;

    public ResourceReader(final ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Reads values from the given resource into the given POJO.
     * @param resourcePath path to the resource
     * @param resourceClass POJO that matches the fields of the given resource
     * @param <T> the given type
     * @return data from the given resource, or an empty {@link Optional} in case of an {@link IOException}
     */
    public <T> Optional<T> readResource(final String resourcePath, final Class<T> resourceClass) {
        try {
            return Optional.of(objectMapper.readValue(getResourceAsStream(resourcePath), resourceClass));
        } catch (final IOException exception) {
            log.error("Failed to read values from {}", resourcePath, exception);
            return Optional.empty();
        }
    }

    protected InputStream getResourceAsStream(final String path) {
        return ResourceReader.class.getResourceAsStream(path);
    }
}
