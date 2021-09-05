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
        final var mainImageConfig = dockerConfig.getMainImageConfig();

        runChainContext.networkExists(checkNetworkExists());
        runChainContext.buildCacheVolumeExists(checkBuildCacheVolumeExists(buildImageConfig));
        log.debug("Checking whether the image exists (name={})", buildImageConfig.getName());
        runChainContext.buildImageExists(dockerClientHelper.imageExists(buildImageConfig.getName()));
        log.debug("Checking whether the container exists (name={})", buildImageConfig.getName());
        runChainContext.buildContainerExists(dockerClientHelper.containerExists(buildImageConfig.getName()));
        log.debug("Checking whether the image exists (name={})", mainImageConfig.getName());
        runChainContext.mainImageExists(dockerClientHelper.imageExists(mainImageConfig.getName()));
        log.debug("Checking whether the container exists (name={})", mainImageConfig.getName());
        runChainContext.mainContainerExists(dockerClientHelper.containerExists(mainImageConfig.getName()));

        return Command.CONTINUE_PROCESSING;
    }

    private boolean checkNetworkExists() {
        final var networkName = dockerConfig.getNetwork();
        log.debug("Checking whether the network already exists (name={})", networkName);
        log.debug("Finding networks by name (name={})", networkName);
        final var networksFiltered = dockerClientHelper.findNetworksByName(networkName);
        log.trace("Networks filtered (name={}):\n{}", networkName, networksFiltered);
        return !networksFiltered.isEmpty();
    }

    private boolean checkBuildCacheVolumeExists(final BuildImageConfig buildImageConfig) {
        final var buildCacheVolumeName = buildImageConfig.getVolume();
        log.debug("Checking whether the build cache volume already exists (name={})", buildCacheVolumeName);
        log.debug("Finding volumes by name (name={})", buildCacheVolumeName);
        final var volumesFiltered = dockerClientHelper.findVolumesByName(buildCacheVolumeName);
        log.trace("Volumes filtered by name (name={}):\n{}", buildCacheVolumeName, volumesFiltered);
        return !volumesFiltered.isEmpty();
    }
}
