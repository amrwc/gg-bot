package dev.amrw.runner.chain.run.command;

import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.Volume;
import dev.amrw.runner.chain.run.RunChainCommandBase;
import dev.amrw.runner.chain.run.RunChainContext;
import dev.amrw.runner.config.BuildImageConfig;
import dev.amrw.runner.service.DockerClientService;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;

/**
 * Creates the build container.
 */
@Log4j2
public class CreateBuildContainer extends RunChainCommandBase {

    public CreateBuildContainer() {
        super();
    }

    public CreateBuildContainer(final DockerClientService dockerClientService) {
        super(dockerClientService);
    }

    @Override
    public boolean execute(final Context context) {
        super.prepareContext(context);
        final var buildImageConfig = dockerConfig.getBuildImageConfig();

        if (runChainContext.buildContainerExists()) {
            final var buildContainerName = buildImageConfig.getName();
            if (args.rebuild()) {
                log.debug("Finding container by name (name={})", buildContainerName);
                dockerClientService.findContainerByName(buildContainerName).ifPresent(container -> {
                    log.info("Removing container (id={}, names={})", container.getId(), container.getNames());
                    getDockerClient().removeContainerCmd(container.getId()).exec();
                });
                // TODO: Remove the Docker image!
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
        final var response = getDockerClient().createContainerCmd(buildImageName)
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
        final var cacheVolume = new Volume(RunChainContext.GRADLE_CACHE_PATH);
        final var volumeBind = new Bind(buildImageConfig.getVolume(), cacheVolume);
        return HostConfig.newHostConfig().withBinds(volumeBind);
    }
}
