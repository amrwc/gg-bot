package dev.amrw.bin.chain.command;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;

import java.util.List;

/**
 * Prepares the Docker environment; creates volumes, networks.
 */
@Log4j2
public class PrepareDockerEnvironment extends RunChainCommand {

    @Override
    public boolean execute(final Context context) {
        super.prepareContext(context);
        createNetwork();
        createVolume();
        return Command.CONTINUE_PROCESSING;
    }

    private void createNetwork() {
        final var networkName = dockerConfig.getNetwork();
        final var networks = dockerClient.listNetworksCmd().withNameFilter(networkName).exec();
        if (networks.isEmpty()) {
            dockerClient.createNetworkCmd().withName(networkName).exec();
        } else {
            log.info("Network already exists, not creating (name={})", networkName);
        }
    }

    private void createVolume() {
        final var volumeName = dockerConfig.getBuildImageConfig().getVolume();
        final var volumes = dockerClient
                .listVolumesCmd()
                .withFilter("name", List.of(volumeName))
                .exec()
                .getVolumes();
        if (volumes.isEmpty()) {
            dockerClient.createVolumeCmd().withName(volumeName).exec();
        } else {
            log.info("Volume already exists, not creating (name={})", volumeName);
        }
    }
}
