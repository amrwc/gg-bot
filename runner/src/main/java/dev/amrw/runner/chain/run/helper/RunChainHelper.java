package dev.amrw.runner.chain.run.helper;

import dev.amrw.runner.config.DockerConfig;
import dev.amrw.runner.helper.DockerClientHelper;
import lombok.extern.log4j.Log4j2;

/**
 * Helper class for the Run Chain.
 */
@Log4j2
public class RunChainHelper {

    private final DockerConfig dockerConfig;
    private final DockerClientHelper dockerClientHelper;

    public RunChainHelper(final DockerConfig dockerConfig, final DockerClientHelper dockerClientHelper) {
        this.dockerConfig = dockerConfig;
        this.dockerClientHelper = dockerClientHelper;
    }

    /**
     * Finds the build container ID. It's useful as it's not obvious when the container ID is going to be used, as some
     * commands may or may not run in any given context, therefore the container ID cannot be easily passed around.
     * @return build container ID
     */
    public String findBuildContainerId() {
        final var buildImageConfig = dockerConfig.getBuildImageConfig();
        final var buildContainerName = buildImageConfig.getName();

        log.debug("Finding containers by name (name={})", buildContainerName);
        final var containers = dockerClientHelper.findContainersByName(buildContainerName);

        log.debug("Fetching the first container from the found list (containers={})", containers);
        final var buildContainer = containers.get(0);

        return buildContainer.getId();
    }
}
