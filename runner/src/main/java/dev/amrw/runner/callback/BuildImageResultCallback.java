package dev.amrw.runner.callback;

import com.github.dockerjava.api.exception.DockerClientException;
import com.github.dockerjava.api.model.BuildResponseItem;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Implementation of {@link com.github.dockerjava.api.command.BuildImageResultCallback} with cleaner output to the CLI.
 */
@Log4j2
public class BuildImageResultCallback extends com.github.dockerjava.api.command.BuildImageResultCallback {

    private String imageId;
    private String error;

    @Override
    public void onNext(final BuildResponseItem item) {
        if (item.isBuildSuccessIndicated()) {
            this.imageId = item.getImageId();
        } else if (Optional.ofNullable(item.getErrorDetail()).isPresent()) {
            this.error = item.getErrorDetail().getMessage();
        }

        final var output = item.getStream();
        if (StringUtils.isNotBlank(output)) {
            System.out.println(output.trim());
        }
    }

    /**
     * Awaits the image ID from the response stream.
     * @throws DockerClientException if the build fails.
     */
    public String awaitImageId() {
        try {
            awaitCompletion();
        } catch (final InterruptedException exception) {
            log.error("Awaiting image ID interrupted", exception);
            throw new DockerClientException("Awaiting image ID interrupted: ", exception);
        }
        return getImageId();
    }

    /**
     * Awaits the image ID from the response stream.
     * @throws DockerClientException if the build fails or the timeout occurs.
     */
    public String awaitImageId(final long timeout, final TimeUnit timeUnit) {
        try {
            awaitCompletion(timeout, timeUnit);
        } catch (final InterruptedException exception) {
            log.error("Awaiting image ID interrupted", exception);
            throw new DockerClientException("Awaiting image ID interrupted: ", exception);
        }
        return getImageId();
    }

    private String getImageId() {
        return Optional.ofNullable(imageId)
                .orElseThrow(() -> {
                    final var error = Optional.ofNullable(this.error)
                            .map(err -> ", " + err)
                            .orElse("");
                    log.error("Could not build image{}", error);
                    return new DockerClientException("Could not build image" + error);
                });
    }
}
