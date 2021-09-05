package dev.amrw.runner.chain.run.command;

import dev.amrw.runner.config.Envar;
import dev.amrw.runner.config.MainImageConfig;
import dev.amrw.runner.exception.InvalidEnvarException;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * Creates the main container.
 */
@Log4j2
public class CreateMainContainer extends RunChainCommand {

    private static final List<String> REQUIRED_ENVARS = List.of(
            Envar.SPRING_DATASOURCE_URL,
            Envar.SPRING_DATASOURCE_USERNAME,
            Envar.SPRING_DATASOURCE_PASSWORD
    );

    // TODO: Add logic to skip this command if the main container already exists, and the rebuild option hasn't been set
    @Override
    public boolean execute(final Context context) {
        super.prepareContext(context);
        final var mainImageConfig = dockerConfig.getMainImageConfig();

        if (runChainContext.mainContainerExists()) {
            final var mainContainerName = mainImageConfig.getName();
            if (args.rebuild()) {
                log.debug("Finding container by name (name={})", mainContainerName);
                dockerClientHelper.findContainerByName(mainContainerName).ifPresent(container -> {
                    log.debug("Removing container (id={}, names={})", container.getId(), container.getNames());
                    dockerClient.removeContainerCmd(container.getId()).exec();
                });
                // TODO: Remove the Docker image!
            } else {
                log.info("Container already exists, not building (name={})", mainContainerName);
                return Command.CONTINUE_PROCESSING;
            }
        }

        final var envars = System.getenv();
        if (!verifyEnvars(envars)) {
            throw new InvalidEnvarException("Missing one or more required envars: " + REQUIRED_ENVARS);
        }

        createContainer(mainImageConfig, envars);
        return Command.CONTINUE_PROCESSING;
    }

    private boolean verifyEnvars(final Map<String, String> envars) {
        return REQUIRED_ENVARS.stream().noneMatch(requiredEnvar ->
                !envars.containsKey(requiredEnvar) || StringUtils.isBlank(envars.get(requiredEnvar)));
    }

    private void createContainer(final MainImageConfig mainImageConfig, final Map<String, String> envars) {
        final var mainImageName = mainImageConfig.getName();
        final var detach = args.detach();

        log.info("Creating container (name={}, detach={})", mainImageName, detach);
        final var response = dockerClient.createContainerCmd(mainImageName)
                .withName(mainImageName)
                .withAttachStdin(detach)
                .withAttachStdout(detach)
                .withAttachStderr(detach)
                .withEnv(List.of(
                        String.format("%s=%s", Envar.SPRING_DATASOURCE_URL, envars.get(Envar.SPRING_DATASOURCE_URL)),
                        String.format("%s=%s",
                                Envar.SPRING_DATASOURCE_USERNAME, envars.get(Envar.SPRING_DATASOURCE_USERNAME)),
                        String.format("%s=%s",
                                Envar.SPRING_DATASOURCE_PASSWORD, envars.get(Envar.SPRING_DATASOURCE_PASSWORD))
                ))
                .exec();
        log.debug("Created container (name={}, id={})", mainImageName, response.getId());
    }
}
