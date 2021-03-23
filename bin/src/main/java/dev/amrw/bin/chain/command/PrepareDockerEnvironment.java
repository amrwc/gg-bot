package dev.amrw.bin.chain.command;

import com.github.dockerjava.api.DockerClient;
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
        final var dockerClient = ((RunChainContext) context).getDockerClient();
        createNetwork(dockerClient);
        createVolume(dockerClient);
        return Command.PROCESSING_COMPLETE;
    }

    private void createNetwork(final DockerClient dockerClient) {
        final var networkName = "ggbot-network";
        final var networks = dockerClient.listNetworksCmd().withNameFilter(networkName).exec();
        if (networks.isEmpty()) {
            dockerClient.createNetworkCmd().withName(networkName).exec();
        } else {
            log.info("Network '{}' already exists, not creating", networkName);
        }
    }

    private void createVolume(final DockerClient dockerClient) {
        final var volumeName = "gradle-build-cache";
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
