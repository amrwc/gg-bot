package dev.amrw.runner.chain.command;

import dev.amrw.runner.util.FileUtil;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;

import java.io.IOException;

/**
 * Copies the compiled JAR file out of the build container.
 */
@Log4j2
public class CopyJarFromBuildContainer extends RunChainCommand {

    private final FileUtil fileUtil;

    public CopyJarFromBuildContainer() {
        this.fileUtil = new FileUtil();
    }

    // TODO: Add logic to skip this step if the main container already exists
    @Override
    public boolean execute(final Context context) throws IOException {
        super.prepareContext(context);

        final var buildContainerId = findContainerId();
        final var pathToJarDir = dockerConfig.getBuildImageConfig().getLibsPath();

        log.info("Copying build archive from build container (id={}, containerPath={})",
                buildContainerId, pathToJarDir);
        final var archiveStream = dockerClient.copyArchiveFromContainerCmd(buildContainerId, pathToJarDir)
                .exec();

        final var binPath = config.getBinPath();
        // Make sure the directory exists, in case it's the first run.
        fileUtil.mkdir(binPath);

        final var archiveName = dockerConfig.getBuildImageConfig().getLibsArchiveName();
        fileUtil.toFile(archiveStream, binPath + "/" + archiveName);

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
