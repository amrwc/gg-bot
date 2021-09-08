package dev.amrw.runner.chain.run.command;

import com.github.dockerjava.api.command.CopyArchiveFromContainerCmd;
import dev.amrw.runner.chain.run.RunChainContext;
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

import static org.apache.commons.lang.RandomStringUtils.randomAlphanumeric;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CopyJarFromBuildContainerTest extends RunChainCommandTestBase {

    @Mock
    private FileUtil fileUtil;
    @InjectMocks
    private CopyJarFromBuildContainer command;

    @Mock
    private BuildImageConfig buildImageConfig;
    @Mock
    private CopyArchiveFromContainerCmd copyArchiveFromContainerCmd;

    private InputStream archiveStream;

    @BeforeEach
    void beforeEach() {
        super.beforeEach();

        final var buildImageName = randomAlphanumeric(16);
        final var buildContainerId = randomAlphanumeric(16);
        archiveStream = mock(InputStream.class);

        when(runChainContext.getBuildImageName()).thenReturn(buildImageName);
        when(dockerClientHelper.findContainerIdByName(buildImageName)).thenReturn(buildContainerId);
        when(dockerClient.copyArchiveFromContainerCmd(buildContainerId, RunChainContext.GRADLE_LIBS_PATH))
                .thenReturn(copyArchiveFromContainerCmd);
        when(copyArchiveFromContainerCmd.exec()).thenReturn(archiveStream);
    }

    @Test
    @DisplayName("Should have rethrown exception when creating `bin` directory")
    void shouldHaveHandledExceptionWhenCreatingBinDir() throws IOException {
        doThrow(new IOException()).when(fileUtil).mkdir(RunChainContext.HOST_BIN_PATH);

        assertThatThrownBy(() -> command.execute(runChainContext)).isInstanceOf(IOException.class);

        verifyNoMoreInteractions(buildImageConfig, fileUtil);
    }

    @Test
    @DisplayName("Should have thrown correct exception when saving `libs` archive to file")
    void shouldHaveHandledExceptionWhenSavingArchiveToFile() throws IOException {

        doThrow(new IOException())
                .when(fileUtil)
                .toFile(archiveStream, RunChainContext.HOST_GRADLE_LIBS_ARCHIVE_PATH);

        assertThatThrownBy(() -> command.execute(runChainContext))
                .isInstanceOf(ContainerArchiveCopyingException.class)
                .hasMessageContainingAll(
                        RunChainContext.GRADLE_LIBS_PATH, RunChainContext.HOST_GRADLE_LIBS_ARCHIVE_PATH);

        verify(fileUtil).mkdir(RunChainContext.HOST_BIN_PATH);
    }

    @Test
    @DisplayName("Should have copied the `libs` archive from the build container")
    void shouldHaveCopiedLibsArchiveFromBuildContainer() throws IOException {
        assertThat(command.execute(runChainContext)).isEqualTo(Command.CONTINUE_PROCESSING);

        verify(fileUtil).mkdir(RunChainContext.HOST_BIN_PATH);
        verify(fileUtil).toFile(archiveStream, RunChainContext.HOST_GRADLE_LIBS_ARCHIVE_PATH);
    }
}
