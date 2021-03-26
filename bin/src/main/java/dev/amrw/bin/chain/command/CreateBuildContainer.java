package dev.amrw.bin.chain.command;

import com.github.dockerjava.api.model.Volume;
import dev.amrw.bin.config.BuildImageConfig;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;

import java.util.Set;

/**
 * Creates the build container.
 */
@Log4j2
public class CreateBuildContainer extends RunChainCommand {

    @Override
    public boolean execute(final Context context) {
        super.prepareContext(context);
        final var buildImageConfig = dockerConfig.getBuildImageConfig();
        final var containers = dockerClient.listContainersCmd()
                .withShowAll(true)
                .withFilter("name", Set.of(buildImageConfig.getName()))
                .exec();

        if (!containers.isEmpty()) {
            if (args.rebuild()) {
                containers.forEach(container -> {
                    log.debug("Removing container (id={}, names={})", container.getId(), container.getNames());
                    dockerClient.removeContainerCmd(container.getId()).exec();
                });
            } else {
                log.info("Container already exists, not building (name={})", buildImageConfig.getName());
                // return Command.CONTINUE_PROCESSING;
                return Command.PROCESSING_COMPLETE; // TEMP: Remove when the next command has been built
            }
        }

        createContainer(buildImageConfig);

        // return Command.CONTINUE_PROCESSING;
        return Command.PROCESSING_COMPLETE; // TEMP: Remove when the next command has been built
    }

    private void createContainer(final BuildImageConfig buildImageConfig) {
        final var buildImageName = buildImageConfig.getName();
        final var detach = args.detach();
        final var cacheVolumePath = String.format("%s:%s",
                buildImageConfig.getVolume(), buildImageConfig.getGradleCachePath());
        final var user = buildImageConfig.getUser();
        final var command = buildImageConfig.getCommand();

        log.info("Creating container (name={}, detach={}, cacheVolume={}, user={}, command={})",
                buildImageName, detach, cacheVolumePath, user, command);
        final var cacheVolume = new Volume(cacheVolumePath);
        dockerClient.createContainerCmd(buildImageName)
                .withName(buildImageName)
                .withVolumes(cacheVolume)
                .withUser(user)
                .withAttachStdin(detach)
                .withAttachStdout(detach)
                .withAttachStderr(detach)
                .withCmd(command)
                .exec();
    }
}