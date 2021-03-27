package dev.amrw.bin.chain.command;

import com.github.dockerjava.api.command.StartContainerCmd;
import com.github.dockerjava.api.command.WaitContainerCmd;
import com.github.dockerjava.api.command.WaitContainerResultCallback;
import com.github.dockerjava.api.model.Container;
import dev.amrw.bin.config.BuildImageConfig;
import org.apache.commons.chain.Command;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.apache.commons.lang3.RandomUtils.nextInt;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StartBuildContainerTest extends RunChainCommandTestBase {

    @InjectMocks
    private StartBuildContainer startBuildContainer;

    @Mock
    private BuildImageConfig buildImageConfig;
    @Mock
    private Container container;

    @Mock
    private StartContainerCmd startContainerCmd;
    @Mock
    private WaitContainerCmd waitContainerCmd;
    @Mock
    private WaitContainerResultCallback waitContainerResultCallback;

    @BeforeEach
    void beforeEach() {
        super.beforeEach();
    }

    @Test
    @DisplayName("Should have started build container")
    void shouldHaveStartedBuildContainer() {
        final var buildContainerName = randomAlphabetic(16);
        final var containerId = randomAlphabetic(16);
        final var statusCode = nextInt();

        when(dockerConfig.getBuildImageConfig()).thenReturn(buildImageConfig);
        when(buildImageConfig.getName()).thenReturn(buildContainerName);
        when(dockerClientHelper.findContainersByName(buildContainerName)).thenReturn(List.of(container));
        when(container.getId()).thenReturn(containerId);

        when(dockerClient.startContainerCmd(containerId)).thenReturn(startContainerCmd);
        when(dockerClient.waitContainerCmd(containerId)).thenReturn(waitContainerCmd);
        when(waitContainerCmd.exec(any(WaitContainerResultCallback.class))).thenReturn(waitContainerResultCallback);
        when(waitContainerResultCallback.awaitStatusCode(2, TimeUnit.MINUTES)).thenReturn(statusCode);

        assertThat(startBuildContainer.execute(runChainContext)).isEqualTo(Command.PROCESSING_COMPLETE);

        verify(startContainerCmd).exec();
    }
}
