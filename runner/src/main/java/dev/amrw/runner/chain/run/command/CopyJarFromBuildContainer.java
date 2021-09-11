package dev.amrw.runner.chain.run.command;

import dev.amrw.runner.chain.run.RunChainCommandBase;
import dev.amrw.runner.chain.run.RunChainContext;
import dev.amrw.runner.exception.ContainerArchiveCopyingException;
import dev.amrw.runner.service.DockerClientService;
import dev.amrw.runner.util.FileUtil;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;

import java.io.IOException;

/**
 * Copies the compiled JAR file out of the build container.
 */
@Log4j2
public class CopyJarFromBuildContainer extends RunChainCommandBase {

    private final FileUtil fileUtil;

    public CopyJarFromBuildContainer() {
        super();
        fileUtil = new FileUtil();
    }

    public CopyJarFromBuildContainer(final DockerClientService dockerClientService, final FileUtil fileUtil) {
        super(dockerClientService);
        this.fileUtil = fileUtil;
    }

    // TODO: Add logic to skip this step if the main container already exists and `--rebuild` is false
    @Override
    public boolean execute(final Context context) throws IOException {
        super.prepareContext(context);

        final var buildImageName = runChainContext.getBuildImageName();
        log.debug("Finding container ID by name (name={})", buildImageName);
        final var buildContainerId = dockerClientService.findContainerIdByName(buildImageName);

        // Copy the entire `libs` directory instead of just the JAR file, because the filename changes depending on the
        // application's version.
        log.info("Copying build archive from build container (id={}, containerPath={})",
                buildContainerId, RunChainContext.GRADLE_LIBS_PATH);
        try (
                // This archive is not actually GZipped, just Tarred.
                final var archiveStream = getDockerClient()
                        .copyArchiveFromContainerCmd(buildContainerId, RunChainContext.GRADLE_LIBS_PATH)
                        .exec();
        ) {
            // Make sure the directory exists, in case it's the first run.
            log.debug("Creating directory (path={})", RunChainContext.HOST_BIN_PATH);
            fileUtil.mkdir(RunChainContext.HOST_BIN_PATH);

            log.debug("Saving InputStream to file (source = {}, destination={})",
                    RunChainContext.GRADLE_LIBS_PATH, RunChainContext.HOST_GRADLE_LIBS_ARCHIVE_PATH);
            fileUtil.toFile(archiveStream, RunChainContext.HOST_GRADLE_LIBS_ARCHIVE_PATH);
        } catch (final IOException exception) {
            throw new ContainerArchiveCopyingException(
                    RunChainContext.GRADLE_LIBS_PATH, RunChainContext.HOST_GRADLE_LIBS_ARCHIVE_PATH);
        }

        return Command.CONTINUE_PROCESSING;
    }
}
