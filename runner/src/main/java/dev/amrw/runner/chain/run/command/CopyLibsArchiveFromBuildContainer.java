package dev.amrw.runner.chain.run.command;

import dev.amrw.runner.exception.ContainerArchiveCopyingException;
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

    // Mocks won't be injected correctly if this is marked as `final`.
    private FileUtil fileUtil;

    public CopyLibsArchiveFromBuildContainer() {
        this.fileUtil = new FileUtil();
    }

    // TODO: Add logic to skip this step if the main container already exists and `--rebuild` is false
    @Override
    public boolean execute(final Context context) throws IOException {
        super.prepareContext(context);

        final var buildImageConfig = dockerConfig.getBuildImageConfig();
        final var buildImageName = buildImageConfig.getName();
        log.debug("Finding container ID by name (name={})", buildImageName);
        final var buildContainerId = dockerClientHelper.findContainerIdByName(buildImageName);
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
            throw exception;
        }

        final var archiveName = buildImageConfig.getLibsArchiveName();
        final var archivePath = binPath + "/" + archiveName;
        try {
            fileUtil.toFile(archiveStream, archivePath);
        } catch (final IOException exception) {
            log.error("Error saving libs archive (path={})", archivePath, exception);
            throw new ContainerArchiveCopyingException(pathToJarDir, archivePath);
        }

        return Command.CONTINUE_PROCESSING;
    }
}
