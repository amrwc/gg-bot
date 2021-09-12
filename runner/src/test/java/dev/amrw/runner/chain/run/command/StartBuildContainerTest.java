package dev.amrw.runner.chain.run.command;

import com.github.dockerjava.api.command.AttachContainerCmd;
import com.github.dockerjava.api.command.StartContainerCmd;
import com.github.dockerjava.api.command.WaitContainerCmd;
import com.github.dockerjava.api.command.WaitContainerResultCallback;
import dev.amrw.runner.callback.FrameResultCallback;
import dev.amrw.runner.chain.run.RunChainContext;
import org.apache.commons.chain.Command;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.TimeUnit;

import static org.apache.commons.lang3.RandomUtils.nextInt;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StartBuildContainerTest extends RunChainCommandTestBase {

    private StartBuildContainer command;

    @Mock
    private StartContainerCmd startContainerCmd;
    @Mock
    private AttachContainerCmd attachContainerCmd;
    @Mock
    private WaitContainerCmd waitContainerCmd;
    @Mock
    private WaitContainerResultCallback waitContainerResultCallback;

    @BeforeEach
    void beforeEach() {
        super.beforeEach();
        command = new StartBuildContainer(dockerClientService);
    }

    @Test
    @DisplayName("Should have started build container")
    void shouldHaveStartedBuildContainer() {
        final var statusCode = nextInt();

        when(dockerClientService.findContainerIdByName(BUILD_IMAGE_NAME)).thenReturn(MAIN_CONTAINER_ID);

        when(dockerClient.startContainerCmd(MAIN_CONTAINER_ID)).thenReturn(startContainerCmd);

        when(dockerClient.attachContainerCmd(MAIN_CONTAINER_ID)).thenReturn(attachContainerCmd);
        when(attachContainerCmd.withStdOut(true)).thenReturn(attachContainerCmd);
        when(attachContainerCmd.withStdErr(true)).thenReturn(attachContainerCmd);
        when(attachContainerCmd.withFollowStream(true)).thenReturn(attachContainerCmd);

        when(dockerClient.waitContainerCmd(MAIN_CONTAINER_ID)).thenReturn(waitContainerCmd);
        when(waitContainerCmd.exec(any(WaitContainerResultCallback.class))).thenReturn(waitContainerResultCallback);
        when(waitContainerResultCallback.awaitStatusCode(2, TimeUnit.MINUTES)).thenReturn(statusCode);

        final var runChainContext = new RunChainContext(config);
        assertThat(command.execute(runChainContext)).isEqualTo(Command.CONTINUE_PROCESSING);

        verify(attachContainerCmd).exec(any(FrameResultCallback.class));
        verify(startContainerCmd).exec();
    }
}
