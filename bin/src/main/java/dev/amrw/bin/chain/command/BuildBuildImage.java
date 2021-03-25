package dev.amrw.bin.chain.command;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.BuildImageResultCallback;
import dev.amrw.bin.chain.context.RunChainContext;
import dev.amrw.bin.config.DockerConfig;
import dev.amrw.bin.dto.RunArgs;
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
        final var args = runChainContext.getArgs();
        final var imageName = runChainContext.getConfig().getDockerConfig().getBuildImage();
        final var dockerClient = runChainContext.getDockerClient();

        // Example repo tags: [ggbot-gradle-build:latest], [<none>:<none>], [postgres:latest], [dpage/pgadmin4:latest]
        // Only keep repo tags that start with the image name
        final var images = dockerClient.listImagesCmd().exec()
                .stream()
                .filter(image -> Arrays.stream(image.getRepoTags()).anyMatch(tag -> tag.startsWith(imageName)))
                .collect(Collectors.toList());

        if (!args.rebuild() && !images.isEmpty()) {
            log.info("Image already exists, not building (name={})", imageName);
            // return Command.CONTINUE_PROCESSING;
            return Command.PROCESSING_COMPLETE; // TEMP: Remove when the next command has been built
        }

        if (args.noCache()) {
            // When the `--no-cache` Docker option for the `build` command has been specified, the existing image with
            // the same tag is going to be re-tagged to `<none>:<none>` and left behind. This is why it's necessary to
            // remove the existing images before building from scratch as to not leave an untagged image behind. It's a
            // build image, therefore there's no significant data to be lost.
            images.forEach(image -> {
                log.debug("Removing image (repoTags={}, id={})", image.getRepoTags(), image.getId());
                dockerClient.removeImageCmd(image.getId()).exec();
            });
        }

        buildImage(dockerClient, imageName, args);

        // return Command.CONTINUE_PROCESSING;
        return Command.PROCESSING_COMPLETE; // TEMP: Remove when the next command has been built
    }

    private void buildImage(final DockerClient dockerClient, final String imageName, final RunArgs args) {
        log.info("Building image (name={})", imageName);
        final var buildImageCmd = dockerClient
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
        log.debug("Created image (name={}, id={})", imageName, imageId);
    }
}
