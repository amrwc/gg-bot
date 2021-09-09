package dev.amrw.runner.chain.run.command;

import dev.amrw.runner.chain.run.RunChainContext;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;

import java.io.IOException;

@Log4j2
public class CopyJarIntoMainContainer extends RunChainCommand {

    // TODO: Skip if the main container already existed before the chain proceeded, and `--rebuild` == false.
    @Override
    public boolean execute(final Context context) throws IOException {
        super.prepareContext(context);

        final var mainImageName = runChainContext.getMainImageName();
        log.debug("Finding container ID by name (name={})", mainImageName);
        final var mainContainerId = dockerClientHelper.findContainerIdByName(mainImageName);

        log.info(
                "Copying application JAR file from host into container" +
                        " (hostResource={}, containerId={}, containerPath={})",
                RunChainContext.HOST_APP_JAR_PATH,
                mainContainerId,
                RunChainContext.REMOTE_PROJECT_PATH
        );
        dockerClient.copyArchiveToContainerCmd(mainContainerId)
                .withHostResource(RunChainContext.HOST_APP_JAR_PATH)
                .withRemotePath(RunChainContext.REMOTE_PROJECT_PATH)
                .exec();

        return Command.CONTINUE_PROCESSING;
    }
}
