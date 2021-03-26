package dev.amrw.bin.chain.command;

import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.command.ListContainersCmd;
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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.apache.commons.lang3.RandomUtils.nextBoolean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateBuildContainerTest extends CommandTestBase {

    @InjectMocks
    private CreateBuildContainer command;

    @Mock
    private BuildImageConfig buildImageConfig;

    @Mock
    private ListContainersCmd listContainersCmd;
    @Mock
    private Container container;
    @Mock
    private RemoveContainerCmd removeContainerCmd;
    @Mock
    private CreateContainerCmd createContainerCmd;

    private String buildImageName;

    @BeforeEach
    void beforeEach() {
        super.beforeEach();
        buildImageName = randomAlphabetic(16);

        when(dockerConfig.getBuildImageConfig()).thenReturn(buildImageConfig);
        when(buildImageConfig.getName()).thenReturn(buildImageName);

        when(dockerClient.listContainersCmd()).thenReturn(listContainersCmd);
        when(listContainersCmd.withShowAll(true)).thenReturn(listContainersCmd);
        when(listContainersCmd.withFilter("name", Set.of(buildImageName))).thenReturn(listContainersCmd);
    }

    @Test
    @DisplayName("Should have skipped creating the build container if it already exists")
    void shouldHaveSkippedWhenContainerExists() {
        when(listContainersCmd.exec()).thenReturn(List.of(container));
        when(args.rebuild()).thenReturn(false);

        assertThat(command.execute(context)).isEqualTo(Command.PROCESSING_COMPLETE);
        verifyNoMoreInteractions(dockerClient);
    }

    @Test
    @DisplayName("Should have created the build container")
    void shouldHaveCreatedContainer() {
        final var cacheVolumePath = addCommonStubs(true);

        assertThat(command.execute(context)).isEqualTo(Command.PROCESSING_COMPLETE);

        verify(removeContainerCmd).exec();
        final var volumeCaptor = ArgumentCaptor.forClass(Volume.class);
        verify(createContainerCmd).withVolumes(volumeCaptor.capture());
        assertThat(volumeCaptor.getValue().getPath()).isEqualTo(cacheVolumePath);
        verify(createContainerCmd).exec();
        verifyNoMoreInteractions(dockerClient);
    }

    @Test
    @DisplayName("Should have created the build container, without removing an existing one")
    void shouldHaveCreatedContainerWithoutRemoving() {
        final var cacheVolumePath = addCommonStubs(false);

        assertThat(command.execute(context)).isEqualTo(Command.PROCESSING_COMPLETE);

        final var volumeCaptor = ArgumentCaptor.forClass(Volume.class);
        verify(createContainerCmd).withVolumes(volumeCaptor.capture());
        assertThat(volumeCaptor.getValue().getPath()).isEqualTo(cacheVolumePath);
        verify(createContainerCmd).exec();
        verifyNoMoreInteractions(dockerClient);
    }

    private String addCommonStubs(final boolean removeExisting) {
        final var containerId = randomAlphanumeric(16);

        final var detach = nextBoolean();
        final var cacheVolumeName = randomAlphanumeric(16);
        final var gradleCachePath = randomAlphanumeric(16);
        final var user = randomAlphanumeric(16);
        final var containerCommand = List.of(randomAlphanumeric(16));

        if (removeExisting) {
            when(listContainersCmd.exec()).thenReturn(List.of(container));
            when(args.rebuild()).thenReturn(true);
            when(container.getId()).thenReturn(containerId);
            when(dockerClient.removeContainerCmd(containerId)).thenReturn(removeContainerCmd);
        } else {
            when(listContainersCmd.exec()).thenReturn(List.of());
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

        return cacheVolumeName + ":" + gradleCachePath;
    }
}
