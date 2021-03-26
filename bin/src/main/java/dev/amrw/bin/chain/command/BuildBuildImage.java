package dev.amrw.bin.chain.command;

import com.github.dockerjava.api.command.BuildImageResultCallback;
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
public class BuildBuildImage extends RunChainCommand {

    @Override
    public boolean execute(final Context context) {
        super.prepareContext(context);
        final var imageName = dockerConfig.getBuildImageConfig().getName();

        // Example repo tags: [ggbot-gradle-build:latest], [<none>:<none>], [postgres:latest], [dpage/pgadmin4:latest]
        // Only keep repo tags that start with the image name
        final var images = dockerClient.listImagesCmd().exec()
                .stream()
                .filter(image -> Arrays.stream(image.getRepoTags()).anyMatch(tag -> tag.startsWith(imageName)))
                .collect(Collectors.toList());

        if (!args.rebuild() && !images.isEmpty()) {
            log.info("Image already exists, not building (name={})", imageName);
            return Command.CONTINUE_PROCESSING;
        }

        if (args.noCache()) {
            // When the `--no-cache` Docker option for the `build` command has been specified, the existing image with
            // the same tag is going to be re-tagged to `<none>:<none>` and left behind (it'll be dangling). This is
            // why it's best to remove the existing images before building from scratch as to not leave an untagged
            // image behind. It's a build image, therefore there's no significant data to be lost.
            images.forEach(image -> {
                log.debug("Removing image (repoTags={}, id={})", image.getRepoTags(), image.getId());
                dockerClient.removeImageCmd(image.getId()).exec();
            });
        }

        buildImage(imageName);

        return Command.CONTINUE_PROCESSING;
    }

    private void buildImage(final String imageName) {
        log.info("Building image (name={})", imageName);
        final var baseDirectory = new File(dockerConfig.getBaseDirPath());
        final var dockerfileGradle = new File(dockerConfig.getDockerfileGradlePath());
        final var buildImageCmd = dockerClient
                .buildImageCmd()
                .withTags(Set.of(imageName))
                .withBaseDirectory(baseDirectory)
                .withDockerfile(dockerfileGradle);

        if (args.noCache()) {
            buildImageCmd.withNoCache(true);
        } else {
            buildImageCmd.withCacheFrom(args.cacheFrom().stream().collect(Collectors.toUnmodifiableSet()));
        }

        final var imageId = buildImageCmd.exec(new BuildImageResultCallback()).awaitImageId();
        log.debug("Created image (name={}, id={})", imageName, imageId);
    }
}
