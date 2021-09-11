package dev.amrw.runner.chain.run.command;

import dev.amrw.runner.chain.run.RunChainCommandBase;
import dev.amrw.runner.chain.run.RunChainContext;
import dev.amrw.runner.service.DockerClientService;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;

import java.io.IOException;

@Log4j2
public class CopyJarIntoMainContainer extends RunChainCommandBase {

    public CopyJarIntoMainContainer() {
        super();
    }

    public CopyJarIntoMainContainer(final DockerClientService dockerClientService) {
        super(dockerClientService);
    }

    // TODO: Skip if the main container already existed before the chain proceeded, and `--rebuild` == false.
    @Override
    public boolean execute(final Context context) throws IOException {
        super.prepareContext(context);

        final var mainImageName = runChainContext.getMainImageName();
        log.debug("Finding container ID by name [name={}]", mainImageName);
        final var mainContainerId = dockerClientService.findContainerIdByName(mainImageName);

        log.info(
                "Copying application JAR file from host into container" +
                        " [hostResource={}, containerId={}, containerPath={}]",
                RunChainContext.HOST_APP_JAR_PATH,
                mainContainerId,
                RunChainContext.REMOTE_PROJECT_PATH
        );
        getDockerClient().copyArchiveToContainerCmd(mainContainerId)
                .withHostResource(RunChainContext.HOST_APP_JAR_PATH)
                .withRemotePath(RunChainContext.REMOTE_PROJECT_PATH)
                .exec();

        return Command.CONTINUE_PROCESSING;
    }
}
