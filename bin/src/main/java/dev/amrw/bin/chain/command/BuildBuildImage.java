package dev.amrw.bin.chain.command;

import com.github.dockerjava.api.command.BuildImageResultCallback;
import dev.amrw.bin.chain.context.RunChainContext;
import dev.amrw.bin.config.DockerConfig;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;

import java.io.File;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Builds the build image.
 */
@Log4j2
public class BuildBuildImage implements Command {

    @Override
    public boolean execute(final Context context) {
        final var runChainContext = (RunChainContext) context;
        final var imageName = runChainContext.getConfig().getDockerConfig().getBuildImage();

        if (!runChainContext.getArgs().rebuild()) {
            // Repo tags examples:
            // [ggbot-gradle-build:latest], [<none>:<none>], [postgres:latest], [dpage/pgadmin4:latest]
            // Only keep repo tags that start with the image name
            final var images = runChainContext.getDockerClient().listImagesCmd().exec()
                    .stream()
                    .filter(image -> Arrays.stream(image.getRepoTags()).anyMatch(tag -> tag.startsWith(imageName)))
                    .collect(Collectors.toList());
            if (!images.isEmpty()) {
                log.info("Image '{}' already exists, not building", imageName);
                // return Command.CONTINUE_PROCESSING;
                return Command.PROCESSING_COMPLETE; // TEMP: Remove when the next command has been built
            }
        }

        buildImage(runChainContext, imageName);

        // return Command.CONTINUE_PROCESSING;
        return Command.PROCESSING_COMPLETE; // TEMP: Remove when the next command has been built
    }

    private void buildImage(final RunChainContext context, final String imageName) {
        log.info("Building '{}' image", imageName);
        final var args = context.getArgs();
        final var buildImageCmd = context.getDockerClient()
                .buildImageCmd()
                .withTags(Set.of(imageName))
                .withBaseDirectory(new File(DockerConfig.BASE_DIR_PATH))
                .withDockerfile(new File(DockerConfig.DOCKERFILE_GRADLE_PATH));

        if (args.noCache()) {
            buildImageCmd.withNoCache(true);
        } else {
            buildImageCmd.withCacheFrom(args.cacheFrom().stream().collect(Collectors.toUnmodifiableSet()));
        }

        final var imageId = buildImageCmd.exec(new BuildImageResultCallback()).awaitImageId();
        log.debug("Created '{}' image with ID '{}'", imageName, imageId);
    }
}
