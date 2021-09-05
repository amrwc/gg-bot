package dev.amrw.runner.chain.run.command;

import com.github.dockerjava.api.command.CopyArchiveFromContainerCmd;
import dev.amrw.runner.config.BuildImageConfig;
import dev.amrw.runner.exception.ContainerArchiveCopyingException;
import dev.amrw.runner.util.FileUtil;
import org.apache.commons.chain.Command;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.io.InputStream;

import static org.apache.commons.lang.RandomStringUtils.randomAlphabetic;
import static org.apache.commons.lang.RandomStringUtils.randomAlphanumeric;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CopyLibsArchiveFromBuildContainerTest extends RunChainCommandTestBase {

    @Mock
    private FileUtil fileUtil;
    @InjectMocks
    private CopyLibsArchiveFromBuildContainer command;

    @Mock
    private BuildImageConfig buildImageConfig;
    @Mock
    private CopyArchiveFromContainerCmd copyArchiveFromContainerCmd;

    private String libsPath;
    private InputStream archiveStream;
    private String binPath;
    private String archiveName;

    @BeforeEach
    void beforeEach() {
        super.beforeEach();

        final var buildContainerName = randomAlphanumeric(16);
        final var buildContainerId = randomAlphanumeric(16);
        libsPath = randomAlphabetic(16);
        archiveStream = mock(InputStream.class);
        binPath = randomAlphabetic(16);
        archiveName = randomAlphabetic(16);

        when(dockerConfig.getBuildImageConfig()).thenReturn(buildImageConfig);
        when(buildImageConfig.getName()).thenReturn(buildContainerName);
        when(dockerClientHelper.findContainerIdByName(buildContainerName)).thenReturn(buildContainerId);
        when(buildImageConfig.getLibsPath()).thenReturn(libsPath);
        when(dockerClient.copyArchiveFromContainerCmd(buildContainerId, libsPath))
                .thenReturn(copyArchiveFromContainerCmd);
        when(copyArchiveFromContainerCmd.exec()).thenReturn(archiveStream);
        when(config.getBinPath()).thenReturn(binPath);
    }

    @Test
    @DisplayName("Should have rethrown exception when creating `bin` directory")
    void shouldHaveHandledExceptionWhenCreatingBinDir() throws IOException {
        doThrow(new IOException()).when(fileUtil).mkdir(binPath);

        assertThatThrownBy(() -> command.execute(runChainContext)).isInstanceOf(IOException.class);

        verifyNoMoreInteractions(buildImageConfig, fileUtil);
    }

    @Test
    @DisplayName("Should have thrown correct exception when saving `libs` archive to file")
    void shouldHaveHandledExceptionWhenSavingArchiveToFile() throws IOException {
        when(buildImageConfig.getLibsArchiveName()).thenReturn(archiveName);
        doThrow(new IOException()).when(fileUtil).toFile(archiveStream, binPath + "/" + archiveName);

        assertThatThrownBy(() -> command.execute(runChainContext))
                .isInstanceOf(ContainerArchiveCopyingException.class)
                .hasMessageContainingAll(libsPath, binPath + "/" + archiveName);

        verify(fileUtil).mkdir(binPath);
    }

    @Test
    @DisplayName("Should have copied the `libs` archive from the build container")
    void shouldHaveCopiedLibsArchiveFromBuildContainer() throws IOException {
        when(buildImageConfig.getLibsArchiveName()).thenReturn(archiveName);

        assertThat(command.execute(runChainContext)).isEqualTo(Command.CONTINUE_PROCESSING);

        verify(fileUtil).mkdir(binPath);
        verify(fileUtil).toFile(archiveStream, binPath + "/" + archiveName);
    }
}
