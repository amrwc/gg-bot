package dev.amrw.runner.chain.run.command;

import com.github.dockerjava.api.command.WaitContainerResultCallback;
import dev.amrw.runner.callback.FrameResultCallback;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;

import java.util.concurrent.TimeUnit;

/**
 * Starts the build container.
 */
@Log4j2
public class StartBuildContainer extends RunChainCommand {

    // TODO: Skip this command when `!args.rebuild()` and the main container already exists
    @Override
    public boolean execute(final Context context) {
        super.prepareContext(context);

        final var containerId = findContainerId();

        log.info("Starting build container (id={})", containerId);
        dockerClient.startContainerCmd(containerId).exec();

        dockerClient.attachContainerCmd(containerId)
                .withStdOut(true)
                .withStdErr(true)
                .withFollowStream(true)
                .exec(new FrameResultCallback());

        log.debug("Awaiting build container to finish running (id={})", containerId);
        final var statusCode = dockerClient.waitContainerCmd(containerId)
                .exec(new WaitContainerResultCallback())
                .awaitStatusCode(2, TimeUnit.MINUTES);
        log.info("Build container finished (statusCode={}, id={})", statusCode, containerId);

        return Command.CONTINUE_PROCESSING;
    }

    private String findContainerId() {
        final var buildImageConfig = dockerConfig.getBuildImageConfig();
        final var buildContainerName = buildImageConfig.getName();
        final var containers = dockerClientHelper.findContainersByName(buildContainerName);
        final var buildContainer = containers.get(0);
        return buildContainer.getId();
    }
}
