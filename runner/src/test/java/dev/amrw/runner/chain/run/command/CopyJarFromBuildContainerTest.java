package dev.amrw.runner.chain.run.command;

import com.github.dockerjava.api.command.CopyArchiveFromContainerCmd;
import dev.amrw.runner.chain.run.RunChainContext;
import dev.amrw.runner.exception.ContainerArchiveCopyingException;
import dev.amrw.runner.service.FileService;
import org.apache.commons.chain.Command;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CopyJarFromBuildContainerTest extends RunChainCommandTestBase {

    @Mock
    private FileService fileService;

    private CopyJarFromBuildContainer command;

    private RunChainContext runChainContext;
    @Mock
    private CopyArchiveFromContainerCmd copyArchiveFromContainerCmd;
    @Mock
    private InputStream archiveStream;

    @BeforeEach
    void beforeEach() {
        super.beforeEach();

        command = new CopyJarFromBuildContainer(dockerClientService, fileService);
        runChainContext = new RunChainContext(config);

        when(dockerClientService.findContainerIdByName(BUILD_IMAGE_NAME)).thenReturn(BUILD_CONTAINER_ID);
        when(dockerClient.copyArchiveFromContainerCmd(BUILD_CONTAINER_ID, RunChainContext.GRADLE_LIBS_PATH))
                .thenReturn(copyArchiveFromContainerCmd);
        when(copyArchiveFromContainerCmd.exec()).thenReturn(archiveStream);
    }

    @Test
    @DisplayName("Should have thrown correct exception when saving `libs` archive to file")
    void shouldHaveHandledExceptionWhenSavingArchiveToFile() throws IOException {
        doThrow(new IOException())
                .when(fileService)
                .toFile(archiveStream, RunChainContext.HOST_GRADLE_LIBS_ARCHIVE_PATH);

        assertThatThrownBy(() -> command.execute(runChainContext))
                .isInstanceOf(ContainerArchiveCopyingException.class)
                .hasMessageContainingAll(
                        RunChainContext.GRADLE_LIBS_PATH, RunChainContext.HOST_GRADLE_LIBS_ARCHIVE_PATH);

        verify(fileService).mkdir(RunChainContext.HOST_BIN_PATH);
    }

    @Test
    @DisplayName("Should have copied the `libs` archive from the build container")
    void shouldHaveCopiedLibsArchiveFromBuildContainer() throws IOException {
        assertThat(command.execute(runChainContext)).isEqualTo(Command.CONTINUE_PROCESSING);

        verify(fileService).mkdir(RunChainContext.HOST_BIN_PATH);
        verify(fileService).toFile(archiveStream, RunChainContext.HOST_GRADLE_LIBS_ARCHIVE_PATH);
    }
}
