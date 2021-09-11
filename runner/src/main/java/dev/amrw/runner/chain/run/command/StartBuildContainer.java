package dev.amrw.runner.chain.run.command;

import com.github.dockerjava.api.command.WaitContainerResultCallback;
import dev.amrw.runner.callback.FrameResultCallback;
import dev.amrw.runner.chain.run.RunChainCommandBase;
import dev.amrw.runner.service.DockerClientService;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;

import java.util.concurrent.TimeUnit;

/**
 * Starts the build container.
 */
@Log4j2
public class StartBuildContainer extends RunChainCommandBase {

    public StartBuildContainer() {
        super();
    }

    public StartBuildContainer(final DockerClientService dockerClientService) {
        super(dockerClientService);
    }

    // TODO: Skip this command when `!args.rebuild()` and the main container already exists
    @Override
    public boolean execute(final Context context) {
        super.prepareContext(context);

        final var buildImageConfig = dockerConfig.getBuildImageConfig();
        final var buildImageName = buildImageConfig.getName();
        log.debug("Finding container ID by name (name={})", buildImageName);
        final var containerId = dockerClientService.findContainerIdByName(buildImageName);

        log.info("Starting build container (id={})", containerId);
        getDockerClient().startContainerCmd(containerId).exec();

        getDockerClient().attachContainerCmd(containerId)
                .withStdOut(true)
                .withStdErr(true)
                .withFollowStream(true)
                .exec(new FrameResultCallback());

        log.debug("Awaiting build container to finish running (id={})", containerId);
        final var statusCode = getDockerClient().waitContainerCmd(containerId)
                .exec(new WaitContainerResultCallback())
                .awaitStatusCode(2, TimeUnit.MINUTES);
        log.info("Build container finished (statusCode={}, id={})", statusCode, containerId);

        return Command.CONTINUE_PROCESSING;
    }
}
