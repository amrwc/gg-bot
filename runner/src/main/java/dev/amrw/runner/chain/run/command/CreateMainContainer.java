package dev.amrw.runner.chain.run.command;

import com.github.dockerjava.api.model.ExposedPort;
import dev.amrw.runner.chain.run.RunChainCommandBase;
import dev.amrw.runner.config.Envar;
import dev.amrw.runner.exception.InvalidEnvarException;
import dev.amrw.runner.service.DockerClientService;
import dev.amrw.runner.service.EnvarsService;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Creates the main container.
 */
@Log4j2
public class CreateMainContainer extends RunChainCommandBase {

    static final List<String> REQUIRED_ENVARS = List.of(
            Envar.SPRING_DATASOURCE_URL,
            Envar.SPRING_DATASOURCE_USERNAME,
            Envar.SPRING_DATASOURCE_PASSWORD
    );

    private final EnvarsService envarsService;

    public CreateMainContainer() {
        super();
        this.envarsService = new EnvarsService();
    }

    public CreateMainContainer(final DockerClientService dockerClientService, final EnvarsService envarsService) {
        super(dockerClientService);
        this.envarsService = envarsService;
    }

    @Override
    public boolean execute(final Context context) {
        super.prepareContext(context);

        final var mainImageName = runChainContext.getMainImageName();
        if (runChainContext.mainContainerExists() && !args.rebuild()) {
            log.info("Container already exists, not building [name={}]", mainImageName);
            return Command.CONTINUE_PROCESSING;
        }

        if (!envarsService.verifyEnvars(REQUIRED_ENVARS)) {
            throw new InvalidEnvarException(
                    "Missing or wrong values of one or more required envars: " + REQUIRED_ENVARS);
        }

        if (runChainContext.mainContainerExists() && args.rebuild()) {
            log.debug("Finding container by name [name={}]", mainImageName);
            dockerClientService.findContainerByName(mainImageName).ifPresent(container -> {
                log.info("Removing container [id={}, names={}]", container.getId(), container.getNames());
                getDockerClient().removeContainerCmd(container.getId()).exec();
            });
            // TODO: Remove the Docker image!
        }

        createContainer();
        connectToNetwork(mainImageName);

        return Command.CONTINUE_PROCESSING;
    }

    private void createContainer() {
        final var mainImageName = runChainContext.getMainImageName();
        final var detach = args.detach();
        final var envars = envarsService.getEnv();
        final var env = buildEnv(envars);

        log.info("Creating container [name={}, detach={}]", mainImageName, detach);
        final var response = getDockerClient().createContainerCmd(mainImageName)
                .withName(mainImageName)
                .withAttachStdin(!detach)
                .withAttachStdout(!detach)
                .withAttachStderr(!detach)
                .withExposedPorts(buildExposedPorts())
                .withEnv(env)
                .exec();
        log.debug("Created container [name={}, id={}]", mainImageName, response.getId());
    }

    private void connectToNetwork(final String mainImageName) {
        log.debug("Finding container ID by name [name={}]", mainImageName);
        final var mainContainerId = dockerClientService.findContainerIdByName(mainImageName);

        final var networkName = dockerConfig.getNetwork();
        log.debug("Finding network ID by name [name={}]", networkName);
        final var networkId = dockerClientService.findNetworkIdByName(networkName);

        log.info("Connecting container to network [containerId={}, networkId={}]", mainContainerId, networkId);
        getDockerClient().connectToNetworkCmd()
                .withNetworkId(networkId)
                .withContainerId(mainContainerId)
                .exec();
    }

    private List<ExposedPort> buildExposedPorts() {
        final var mainImageConfig = dockerConfig.getMainImageConfig();
        final List<ExposedPort> exposedPorts = new ArrayList<>();
        exposedPorts.add(ExposedPort.tcp(mainImageConfig.getPort()));
        if (args.debug()) {
            exposedPorts.add(ExposedPort.tcp(mainImageConfig.getDebugPort()));
        }
        return exposedPorts;
    }

    private List<String> buildEnv(final Map<String, String> envars) {
        return REQUIRED_ENVARS.stream()
                .map(envarName -> envarName + "=" + envars.get(envarName))
                .collect(Collectors.toList());
    }
}
