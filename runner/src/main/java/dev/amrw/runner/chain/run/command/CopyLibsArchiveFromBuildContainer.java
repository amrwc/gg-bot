package dev.amrw.runner.chain.run.command;

import dev.amrw.runner.util.FileUtil;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;

import java.io.IOException;

/**
 * Copies the compiled JAR file out of the build container.
 */
@Log4j2
public class CopyLibsArchiveFromBuildContainer extends RunChainCommand {

    private FileUtil fileUtil;

    public CopyLibsArchiveFromBuildContainer() {
        this.fileUtil = new FileUtil();
    }

    // TODO: Add logic to skip this step if the main container already exists
    @Override
    public boolean execute(final Context context) {
        super.prepareContext(context);

        final var runChainHelper = getRunChainHelper();
        final var buildContainerId = runChainHelper.findBuildContainerId();
        final var buildImageConfig = dockerConfig.getBuildImageConfig();
        final var pathToJarDir = buildImageConfig.getLibsPath();

        log.info("Copying build archive from build container (id={}, containerPath={})",
                buildContainerId, pathToJarDir);
        final var archiveStream = dockerClient.copyArchiveFromContainerCmd(buildContainerId, pathToJarDir)
                .exec();

        final var binPath = config.getBinPath();
        try {
            // Make sure the directory exists, in case it's the first run.
            fileUtil.mkdir(binPath);
        } catch (final IOException exception) {
            log.error("Error creating bin directory", exception);
            return Command.PROCESSING_COMPLETE;
        }

        final var archiveName = buildImageConfig.getLibsArchiveName();
        try {
            fileUtil.toFile(archiveStream, binPath + "/" + archiveName);
        } catch (final IOException exception) {
            log.error("Error creating bin directory", exception);
            return Command.PROCESSING_COMPLETE;
        }

        // return Command.CONTINUE_PROCESSING;
        return Command.PROCESSING_COMPLETE; // TEMP: Remove when the next command has been built
    }
}
