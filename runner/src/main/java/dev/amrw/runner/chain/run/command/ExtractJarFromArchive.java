package dev.amrw.runner.chain.run.command;

import dev.amrw.runner.chain.run.RunChainCommandBase;
import dev.amrw.runner.chain.run.RunChainContext;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.utils.IOUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Log4j2
public class ExtractJarFromArchive extends RunChainCommandBase {

    // TODO: Skip if the main container already existed before the chain proceeded, and `--rebuild` == false.
    @Override
    public boolean execute(final Context context) throws Exception {
        super.prepareContext(context);

        // The archive is not GZipped, only Tarred.
        log.info("Extracting Gradle `libs` archive (path={})", RunChainContext.HOST_GRADLE_LIBS_ARCHIVE_PATH);
        final var hostGradleLibsTarGzArchivePath = Path.of(RunChainContext.HOST_GRADLE_LIBS_ARCHIVE_PATH);
        try (
                final var tarInputStream = Files.newInputStream(hostGradleLibsTarGzArchivePath);
                final var archiveInputStream = (TarArchiveInputStream) new ArchiveStreamFactory()
                        .createArchiveInputStream("tar", tarInputStream);
        ) {
            // https://stackoverflow.com/a/7556307/10620237
            TarArchiveEntry entry;
            while ((entry = (TarArchiveEntry) archiveInputStream.getNextEntry()) != null) {
                final var outputFile = new File(RunChainContext.HOST_BIN_PATH, entry.getName());
                if (entry.isDirectory()) {
                    log.debug("Writing output directory (path={})", outputFile.getAbsolutePath());
                    if (!outputFile.exists()) {
                        log.debug("Creating output directory (path={})", outputFile.getAbsolutePath());
                        if (!outputFile.mkdirs()) {
                            throw new IllegalStateException(
                                    "Couldn't create directory " + outputFile.getAbsolutePath());
                        }
                    }
                } else {
                    log.debug("Creating output file (path={})", outputFile.getAbsolutePath());
                    try (final var outputFileStream = Files.newOutputStream(outputFile.toPath())) {
                        IOUtils.copy(archiveInputStream, outputFileStream);
                    }
                }
            }
        }

        log.debug("Finding JAR file in the extracted directory (path={})", RunChainContext.HOST_BIN_LIBS_PATH);
        final var hostBinLibsPath = Path.of(RunChainContext.HOST_BIN_LIBS_PATH);
        final String jarPath;
        try (final var walk = Files.walk(hostBinLibsPath)) {
            jarPath = walk
                    .filter(path -> !Files.isDirectory(path))
                    .map(path -> path.toString().toLowerCase())
                    .filter(path -> path.endsWith(".jar"))
                    .findFirst()
                    .orElseThrow(() -> new FileNotFoundException("No file with `.jar` extension found"));
        }
        log.debug("Copying JAR file out of the extracted directory (sourcePath={}, destinationPath={})",
                jarPath, RunChainContext.HOST_APP_JAR_PATH);
        Files.copy(Paths.get(jarPath), Paths.get(RunChainContext.HOST_APP_JAR_PATH));

        return Command.CONTINUE_PROCESSING;
    }
}
