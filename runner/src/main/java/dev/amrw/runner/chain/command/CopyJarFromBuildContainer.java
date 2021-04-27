package dev.amrw.runner.chain.command;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

/**
 * Copies the compiled JAR file out of the build container.
 */
@Log4j2
public class CopyJarFromBuildContainer extends RunChainCommand {

    // TODO: Add logic to skip this step if the main container already exists
    @Override
    public boolean execute(final Context context) throws IOException {
        super.prepareContext(context);

        final var buildContainerId = findContainerId();
        final var pathToJarDir = dockerConfig.getBuildImageConfig().getLibsPath();

        log.info("Copying build archive from build container (id={}, containerPath={})",
                buildContainerId, pathToJarDir);
        final var stream = dockerClient.copyArchiveFromContainerCmd(buildContainerId, pathToJarDir)
                .exec();

        // TODO: Extract the path to the config file
        final var hostPath = "bin";
        log.info("Creating directory on host machine (path={})", hostPath);
        final var hostTargetDirectory = new File(hostPath);
        FileUtils.forceMkdir(hostTargetDirectory);

        // TODO: Extract to a constant somewhere
        final var hostTargetFile = new File(hostTargetDirectory + "/archive.tar.gz");
        FileUtils.copyInputStreamToFile(stream, hostTargetFile);

        // return Command.CONTINUE_PROCESSING;
        return Command.PROCESSING_COMPLETE; // TEMP: Remove when the next command has been built
    }

    private String findContainerId() {
        final var buildImageConfig = dockerConfig.getBuildImageConfig();
        final var buildContainerName = buildImageConfig.getName();
        final var containers = dockerClientHelper.findContainersByName(buildContainerName);
        final var buildContainer = containers.get(0);
        return buildContainer.getId();
    }
}
