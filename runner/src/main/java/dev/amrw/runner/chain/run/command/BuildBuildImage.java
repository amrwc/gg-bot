package dev.amrw.runner.chain.run.command;

import dev.amrw.runner.callback.BuildImageResultCallback;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;

import java.io.File;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Builds the build image.
 */
@Log4j2
public class BuildBuildImage extends RunChainCommand {

    // TODO: DRY the code here and in `BuildMainImage`.
    @Override
    public boolean execute(final Context context) {
        super.prepareContext(context);
        final var buildImageName = dockerConfig.getBuildImageConfig().getName();

        if (!args.rebuild() && runChainContext.buildImageExists()) {
            log.info("Image already exists, not building (name={})", buildImageName);
            return Command.CONTINUE_PROCESSING;
        }

        if (args.noCache()) {
            // When the `--no-cache` Docker option for the `build` command has been specified, the existing image with
            // the same tag is going to be re-tagged to `<none>:<none>` and left behind (it'll be dangling). This is
            // why it's best to remove the existing images before building from scratch as to not leave an untagged
            // image behind.
            final var images = dockerClientHelper.findImagesByName(buildImageName);
            images.forEach(image -> {
                log.debug("Removing image (repoTags={}, id={})", image.getRepoTags(), image.getId());
                dockerClient.removeImageCmd(image.getId()).exec();
            });
        }

        buildImage(buildImageName);

        return Command.CONTINUE_PROCESSING;
    }

    private void buildImage(final String imageName) {
        log.info("Building image (name={})", imageName);
        final var baseDirectory = new File(dockerConfig.getBaseDirPath());
        log.trace("Docker base directory: {}", baseDirectory);
        final var dockerfileGradle = new File(dockerConfig.getDockerfileGradlePath());
        log.trace("Dockerfile-gradle: {}", dockerfileGradle);
        final var buildImageCmd = dockerClient.buildImageCmd()
                // NOTE: There's a dependency on `baseDirectory` inside `withDockerfile()`, therefore
                // `withBaseDirectory()` _must_ come before `withDockerfile()`, if applicable. Otherwise, this error
                // may be thrown:
                // > Dockerfile is excluded by pattern '*' in .dockerignore file
                .withTags(Set.of(imageName))
                .withBaseDirectory(baseDirectory)
                .withDockerfile(dockerfileGradle);

        if (args.noCache()) {
            buildImageCmd.withNoCache(true);
        } else {
            log.trace("Caching from:\n{}", args.cacheFrom());
            buildImageCmd.withCacheFrom(args.cacheFrom().stream().collect(Collectors.toUnmodifiableSet()));
        }

        final var imageId = buildImageCmd.exec(new BuildImageResultCallback()).awaitImageId();
        log.debug("Created image (name={}, id={})", imageName, imageId);
    }
}
