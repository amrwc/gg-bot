package dev.amrw.runner.chain.run.command;

import dev.amrw.runner.config.BuildImageConfig;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;

/**
 * Checks which required components exist before the chain starts to simplify logic in the individual commands.
 */
@Log4j2
public class CheckDockerComponents extends RunChainCommand {

    @Override
    public boolean execute(final Context context) {
        super.prepareContext(context);

        final var buildImageConfig = dockerConfig.getBuildImageConfig();

        runChainContext.networkExists(checkNetworkExists());
        runChainContext.buildCacheVolumeExists(checkBuildCacheVolumeExists(buildImageConfig));
        runChainContext.buildImageExists(checkBuildImageExists(buildImageConfig));
        runChainContext.buildContainerExists(checkBuildContainerExists(buildImageConfig));

        return Command.CONTINUE_PROCESSING;
    }

    private boolean checkNetworkExists() {
        final var networkName = dockerConfig.getNetwork();
        log.debug("Checking whether the network already exists (name={})", networkName);
        final var networksFiltered = dockerClientHelper.findNetworksByName(networkName);
        log.trace("Networks filtered (name={}):\n{}", networkName, networksFiltered);
        return !networksFiltered.isEmpty();
    }

    private boolean checkBuildCacheVolumeExists(final BuildImageConfig buildImageConfig) {
        final var buildCacheVolumeName = buildImageConfig.getVolume();
        log.debug("Checking whether the build cache volume already exists (name={})", buildCacheVolumeName);
        final var volumesFiltered = dockerClientHelper.findVolumesByName(buildCacheVolumeName);
        log.trace("Volumes filtered by name (name={}):\n{}", buildCacheVolumeName, volumesFiltered);
        return !volumesFiltered.isEmpty();
    }

    private boolean checkBuildImageExists(final BuildImageConfig buildImageConfig) {
        final var buildImageName = buildImageConfig.getName();
        log.debug("Checking whether the build image already exists (name={})", buildImageName);
        final var imagesFiltered = dockerClientHelper.findImagesByName(buildImageName);
        log.trace("Images filtered by name (name={}):\n{}", buildImageName, imagesFiltered);
        return !imagesFiltered.isEmpty();
    }

    private boolean checkBuildContainerExists(final BuildImageConfig buildImageConfig) {
        final var buildContainerName = buildImageConfig.getName();
        log.debug("Checking whether the build container already exists (name={})", buildContainerName);
        final var containers = dockerClientHelper.findContainersByName(buildContainerName);
        log.trace("Containers filtered by name (name={}):\n{}", buildContainerName, containers);
        return !containers.isEmpty();
    }
}
