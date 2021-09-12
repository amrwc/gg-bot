package dev.amrw.runner.chain.run.command;

import com.github.dockerjava.api.command.CopyArchiveToContainerCmd;
import dev.amrw.runner.chain.run.RunChainContext;
import org.apache.commons.chain.Command;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CopyJarIntoMainContainerTest extends RunChainCommandTestBase {

    private CopyJarIntoMainContainer command;

    @Mock
    private CopyArchiveToContainerCmd copyArchiveToContainerCmd;

    @BeforeEach
    void beforeEach() {
        super.beforeEach();
        command = new CopyJarIntoMainContainer(dockerClientService);
    }

    @Test
    @DisplayName("Should have copied JAR file into the main container")
    void shouldHaveCopiedJarIntoMainContainer() throws IOException {
        when(dockerClientService.findContainerIdByName(MAIN_IMAGE_NAME)).thenReturn(MAIN_CONTAINER_ID);
        when(dockerClient.copyArchiveToContainerCmd(MAIN_CONTAINER_ID)).thenReturn(copyArchiveToContainerCmd);
        when(copyArchiveToContainerCmd.withHostResource(RunChainContext.HOST_APP_JAR_PATH))
                .thenReturn(copyArchiveToContainerCmd);
        when(copyArchiveToContainerCmd.withRemotePath(RunChainContext.REMOTE_PROJECT_PATH))
                .thenReturn(copyArchiveToContainerCmd);

        final var runChainContext = new RunChainContext(config);
        assertThat(command.execute(runChainContext)).isEqualTo(Command.CONTINUE_PROCESSING);

        verify(copyArchiveToContainerCmd).exec();
    }
}
