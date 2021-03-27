package dev.amrw.bin.chain.command;

import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.RemoveContainerCmd;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.Volume;
import dev.amrw.bin.config.BuildImageConfig;
import org.apache.commons.chain.Command;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.apache.commons.lang3.RandomUtils.nextBoolean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateBuildContainerTest extends RunChainCommandTestBase {

    @InjectMocks
    private CreateBuildContainer createBuildContainer;

    @Mock
    private BuildImageConfig buildImageConfig;

    @Mock
    private Container container;
    @Mock
    private RemoveContainerCmd removeContainerCmd;
    @Mock
    private CreateContainerCmd createContainerCmd;

    @Captor
    private ArgumentCaptor<Volume> volumeCaptor;

    private String buildImageName;

    @BeforeEach
    void beforeEach() {
        super.beforeEach();
        buildImageName = randomAlphabetic(16);

        when(dockerConfig.getBuildImageConfig()).thenReturn(buildImageConfig);
        when(buildImageConfig.getName()).thenReturn(buildImageName);
    }

    @Test
    @DisplayName("Should have skipped creating the build container if it already exists")
    void shouldHaveSkippedWhenContainerExists() {
        when(runChainContext.buildContainerExists()).thenReturn(true);
        when(args.rebuild()).thenReturn(false);

        assertThat(createBuildContainer.execute(runChainContext)).isEqualTo(Command.CONTINUE_PROCESSING);

        verifyNoMoreInteractions(dockerClient);
    }

    @Test
    @DisplayName("Should have created the build container")
    void shouldHaveCreatedContainer() {
        final var cacheVolumePath = addCommonStubs(true);

        assertThat(createBuildContainer.execute(runChainContext)).isEqualTo(Command.CONTINUE_PROCESSING);

        verify(removeContainerCmd).exec();
        verify(createContainerCmd).withVolumes(volumeCaptor.capture());
        assertThat(volumeCaptor.getValue().getPath()).isEqualTo(cacheVolumePath);
        verifyNoMoreInteractions(dockerClient);
    }

    @Test
    @DisplayName("Should have created the build container, without removing an existing one")
    void shouldHaveCreatedContainerWithoutRemoving() {
        final var cacheVolumePath = addCommonStubs(false);

        assertThat(createBuildContainer.execute(runChainContext)).isEqualTo(Command.CONTINUE_PROCESSING);

        verify(createContainerCmd).withVolumes(volumeCaptor.capture());
        assertThat(volumeCaptor.getValue().getPath()).isEqualTo(cacheVolumePath);
        verifyNoMoreInteractions(dockerClient);
    }

    private String addCommonStubs(final boolean buildContainerExists) {
        final var containerId = randomAlphanumeric(16);

        final var detach = nextBoolean();
        final var cacheVolumeName = randomAlphanumeric(16);
        final var gradleCachePath = randomAlphanumeric(16);
        final var user = randomAlphanumeric(16);
        final var containerCommand = List.of(randomAlphanumeric(16));

        when(runChainContext.buildContainerExists()).thenReturn(buildContainerExists);
        if (buildContainerExists) {
            when(dockerClientHelper.findContainersByName(buildImageName)).thenReturn(List.of(container));
            when(args.rebuild()).thenReturn(true);
            when(container.getId()).thenReturn(containerId);
            when(dockerClient.removeContainerCmd(containerId)).thenReturn(removeContainerCmd);
        }

        when(args.detach()).thenReturn(detach);
        when(buildImageConfig.getVolume()).thenReturn(cacheVolumeName);
        when(buildImageConfig.getGradleCachePath()).thenReturn(gradleCachePath);
        when(buildImageConfig.getUser()).thenReturn(user);
        when(buildImageConfig.getCommand()).thenReturn(containerCommand);

        when(dockerClient.createContainerCmd(buildImageName)).thenReturn(createContainerCmd);
        when(createContainerCmd.withName(buildImageName)).thenReturn(createContainerCmd);
        when(createContainerCmd.withVolumes(any(Volume.class))).thenReturn(createContainerCmd);
        when(createContainerCmd.withUser(user)).thenReturn(createContainerCmd);
        when(createContainerCmd.withAttachStdin(detach)).thenReturn(createContainerCmd);
        when(createContainerCmd.withAttachStdout(detach)).thenReturn(createContainerCmd);
        when(createContainerCmd.withAttachStderr(detach)).thenReturn(createContainerCmd);
        when(createContainerCmd.withCmd(containerCommand)).thenReturn(createContainerCmd);
        when(createContainerCmd.exec()).thenReturn(mock(CreateContainerResponse.class));

        return cacheVolumeName + ":" + gradleCachePath;
    }
}
