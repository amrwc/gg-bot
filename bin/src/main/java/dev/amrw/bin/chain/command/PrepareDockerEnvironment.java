package dev.amrw.bin.chain.command;

import dev.amrw.bin.chain.context.RunChainContext;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;

import java.util.List;

/**
 * Prepares the Docker environment; creates volumes, networks.
 */
@Log4j2
public class PrepareDockerEnvironment implements Command {

    @Override
    public boolean execute(final Context context) {
        final var runChainContext = (RunChainContext) context;
        createNetwork(runChainContext);
        createVolume(runChainContext);
        return Command.CONTINUE_PROCESSING;
    }

    private void createNetwork(final RunChainContext context) {
        final var dockerClient = context.getDockerClient();
        final var networkName = context.getConfig().getDockerConfig().getNetwork();
        final var networks = dockerClient.listNetworksCmd().withNameFilter(networkName).exec();
        if (networks.isEmpty()) {
            dockerClient.createNetworkCmd().withName(networkName).exec();
        } else {
            log.info("Network '{}' already exists, not creating", networkName);
        }
    }

    private void createVolume(final RunChainContext context) {
        final var dockerClient = context.getDockerClient();
        final var volumeName = context.getConfig().getDockerConfig().getCacheVolume();
        final var volumes = dockerClient
                .listVolumesCmd()
                .withFilter("name", List.of(volumeName))
                .exec()
                .getVolumes();
        if (volumes.isEmpty()) {
            dockerClient.createVolumeCmd().withName(volumeName).exec();
        } else {
            log.info("Volume '{}' already exists, not creating", volumeName);
        }
    }
}
