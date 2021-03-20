package dev.amrw.ggbot.util;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.InputStream;
import java.util.Optional;

/**
 * Utility for working with resources on classpath.
 */
@Log4j2
@Component
public class ResourceUtil {

    /**
     * Gets the given resource as {@link File}.
     * @param callerClass caller's class
     * @param resourcePath path to the resource
     * @param <T> caller's type
     * @return {@link File} of the resource
     */
    public <T> Optional<File> getResourceAsFile(final Class<T> callerClass, final String resourcePath) {
        try {
            final var resource = callerClass.getResource(resourcePath);
            final var uri = resource.toURI();
            return Optional.of(new File(uri));
        } catch (final Exception exception) {
            log.error("Error getting resource as File. callerClass={}, resourcePath={}",
                    callerClass, resourcePath, exception);
            return Optional.empty();
        }
    }

    /**
     * Gets the given resource as {@link InputStream}.
     * @param callerClass caller's class
     * @param resourcePath path to the resource
     * @param <T> caller's type
     * @return {@link InputStream} of the resource
     */
    public <T> InputStream getResourceAsStream(final Class<T> callerClass, final String resourcePath) {
        return callerClass.getResourceAsStream(resourcePath);
    }
}
