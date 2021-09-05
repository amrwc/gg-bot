package dev.amrw.runner.chain.run.command;

import com.github.dockerjava.api.command.WaitContainerResultCallback;
import com.github.dockerjava.api.model.Container;
import dev.amrw.runner.callback.FrameResultCallback;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.concurrent.TimeUnit;

/**
 * Starts the main container.
 */
@Log4j2
public class StartMainContainer extends RunChainCommand {

    @Override
    public boolean execute(final Context context) throws IOException {
        super.prepareContext(context);

        final var mainImageConfig = dockerConfig.getMainImageConfig();
        final var mainImageName = mainImageConfig.getName();
        log.debug("Finding container ID by name (name={})", mainImageName);
        final var containerId = dockerClientHelper.findContainerIdByName(mainImageName);

        log.info("Starting main container (id={})", containerId);
        dockerClient.startContainerCmd(containerId).exec();

        if (!args.detach()) {
            final var outputStream = new PipedOutputStream();
            final var inputStream = new PipedInputStream(outputStream);
            dockerClient.attachContainerCmd(containerId)
                    .withStdIn(inputStream)
                    .withStdOut(true)
                    .withStdErr(true)
                    .withFollowStream(true)
                    .exec(new FrameResultCallback());
        }

        log.debug("Awaiting main container to finish running (id={})", containerId);
        final var statusCode = dockerClient.waitContainerCmd(containerId)
                .exec(new WaitContainerResultCallback())
                .awaitStatusCode(2, TimeUnit.MINUTES);
        log.info("Main container finished (statusCode={}, id={})", statusCode, containerId);

        return Command.PROCESSING_COMPLETE;
    }
}
