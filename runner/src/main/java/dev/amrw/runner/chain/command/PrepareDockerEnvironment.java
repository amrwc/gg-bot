package dev.amrw.runner.chain.command;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;

/**
 * Prepares the Docker environment; creates volumes, networks.
 */
@Log4j2
public class PrepareDockerEnvironment extends RunChainCommand {

    @Override
    public boolean execute(final Context context) {
        super.prepareContext(context);
        createNetwork();
        createBuildCacheVolume();
        return Command.CONTINUE_PROCESSING;
    }

    private void createNetwork() {
        final var networkName = dockerConfig.getNetwork();
        if (runChainContext.networkExists()) {
            log.info("Network already exists, not creating (name={})", networkName);
        } else {
            log.info("Creating network (name={})", networkName);
            dockerClient.createNetworkCmd().withName(networkName).exec();
        }
    }

    private void createBuildCacheVolume() {
        final var buildImageConfig = dockerConfig.getBuildImageConfig();
        final var volumeName = buildImageConfig.getVolume();
        if (runChainContext.buildCacheVolumeExists()) {
            log.info("Volume already exists, not creating (name={})", volumeName);
        } else {
            log.info("Creating volume (name={})", volumeName);
            dockerClient.createVolumeCmd().withName(volumeName).exec();
        }
    }
}
