package dev.amrw.bin.chain.command;

import com.github.dockerjava.api.model.Volume;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;

import java.util.Set;

/**
 * Creates the build container.
 */
@Log4j2
public class CreateBuildContainer extends RunChainCommand {

    /** Location of Gradle cache inside the Docker container. */
    private static final String GRADLE_CACHE_PATH = "/home/gradle/.gradle";

    @Override
    public boolean execute(final Context context) {
        super.prepareContext(context);
        final var buildImageName = dockerConfig.getBuildImageConfig().getName();
        final var cacheVolumeName = dockerConfig.getBuildImageConfig().getVolume();
        final var cacheVolumePath = String.format("%s:%s", cacheVolumeName, GRADLE_CACHE_PATH);
        final var user = dockerConfig.getBuildImageConfig().getUser();
        final var command = dockerConfig.getBuildImageConfig().getCommand();

        final var containers = dockerClient.listContainersCmd()
                .withShowAll(true)
                .withFilter("name", Set.of(buildImageName))
                .exec();
        if (!containers.isEmpty()) {
            if (args.rebuild()) {
                containers.forEach(container -> {
                    log.debug("Removing container (id={}, names={})", container.getId(), container.getNames());
                    dockerClient.removeContainerCmd(container.getId()).exec();
                });
            } else {
                log.info("Container already exists, not building (name={})", buildImageName);
                // return Command.CONTINUE_PROCESSING;
                return Command.PROCESSING_COMPLETE; // TEMP: Remove when the next command has been built
            }
        }

        log.info("Creating container (name={}, detach={}, cacheVolume={}, user={}, command={})",
                buildImageName, args.detach(), cacheVolumePath, user, command);
        final var cacheVolume = new Volume(cacheVolumePath);
        dockerClient.createContainerCmd(buildImageName)
                .withName(buildImageName)
                .withVolumes(cacheVolume)
                .withUser(user)
                .withAttachStdin(args.detach())
                .withAttachStdout(args.detach())
                .withAttachStderr(args.detach())
                .withCmd(command)
                .exec();

        // return Command.CONTINUE_PROCESSING;
        return Command.PROCESSING_COMPLETE; // TEMP: Remove when the next command has been built
    }
}
