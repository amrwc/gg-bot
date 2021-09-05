package dev.amrw.runner.chain.run.command;

import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.Volume;
import dev.amrw.runner.config.BuildImageConfig;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;

/**
 * Creates the build container.
 */
@Log4j2
public class CreateBuildContainer extends RunChainCommand {

    @Override
    public boolean execute(final Context context) {
        super.prepareContext(context);
        final var buildImageConfig = dockerConfig.getBuildImageConfig();

        if (runChainContext.buildContainerExists()) {
            final var buildContainerName = buildImageConfig.getName();
            if (args.rebuild()) {
                log.debug("Finding containers by name (name={})", buildContainerName);
                final var containers = dockerClientHelper.findContainersByName(buildContainerName);
                containers.forEach(container -> {
                    log.debug("Removing container (id={}, names={})", container.getId(), container.getNames());
                    dockerClient.removeContainerCmd(container.getId()).exec();
                });
            } else {
                log.info("Container already exists, not building (name={})", buildContainerName);
                return Command.CONTINUE_PROCESSING;
            }
        }

        createContainer(buildImageConfig);
        return Command.CONTINUE_PROCESSING;
    }

    private void createContainer(final BuildImageConfig buildImageConfig) {
        final var buildImageName = buildImageConfig.getName();
        final var detach = args.detach();
        final var user = buildImageConfig.getUser();
        final var command = buildImageConfig.getCommand();

        log.info("Creating container (name={}, detach={}, cacheVolume={}, user={}, command={})",
                buildImageName, detach, buildImageConfig.getVolume(), user, command);

        final var hostConfig = buildHostConfig(buildImageConfig);
        final var response = dockerClient.createContainerCmd(buildImageName)
                .withName(buildImageName)
                .withHostConfig(hostConfig)
                .withUser(user)
                .withAttachStdin(detach)
                .withAttachStdout(detach)
                .withAttachStderr(detach)
                .withCmd(command)
                .exec();
        log.debug("Created container (name={}, id={})", buildImageName, response.getId());
    }

    private HostConfig buildHostConfig(final BuildImageConfig buildImageConfig) {
        final var cacheVolume = new Volume(buildImageConfig.getGradleCachePath());
        final var volumeBind = new Bind(buildImageConfig.getVolume(), cacheVolume);
        return HostConfig.newHostConfig().withBinds(volumeBind);
    }
}
