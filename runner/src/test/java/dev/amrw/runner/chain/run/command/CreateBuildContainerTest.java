package dev.amrw.runner.chain.run.command;

import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.RemoveContainerCmd;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.HostConfig;
import dev.amrw.runner.chain.run.RunChainContext;
import dev.amrw.runner.config.BuildImageConfig;
import org.apache.commons.chain.Command;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.apache.commons.lang3.RandomUtils.nextBoolean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateBuildContainerTest extends RunChainCommandTestBase {

    private CreateBuildContainer command;

    @Mock
    private BuildImageConfig buildImageConfig;

    @Mock
    private Container container;
    @Mock
    private RemoveContainerCmd removeContainerCmd;
    @Mock
    private CreateContainerCmd createContainerCmd;

    @Captor
    private ArgumentCaptor<HostConfig> hostConfigCaptor;

    @BeforeEach
    void beforeEach() {
        super.beforeEach();

        command = new CreateBuildContainer(dockerClientService);

        when(dockerConfig.getBuildImageConfig()).thenReturn(buildImageConfig);
        when(buildImageConfig.getName()).thenReturn(BUILD_IMAGE_NAME);
    }

    @Test
    @DisplayName("Should have skipped creating the build container if it already exists")
    void shouldHaveSkippedWhenContainerExists() {
        when(runChainContext.buildContainerExists()).thenReturn(true);
        when(args.rebuild()).thenReturn(false);

        assertThat(command.execute(runChainContext)).isEqualTo(Command.CONTINUE_PROCESSING);

        verifyNoInteractions(dockerClient);
    }

    @Test
    @DisplayName("Should have created the build container")
    void shouldHaveCreatedContainer() {
        addCommonStubs(true);

        assertThat(command.execute(runChainContext)).isEqualTo(Command.CONTINUE_PROCESSING);

        verify(removeContainerCmd).exec();
        addCommonVerifications();
    }

    @Test
    @DisplayName("Should have created the build container, without removing an existing one")
    void shouldHaveCreatedContainerWithoutRemoving() {
        addCommonStubs(false);

        assertThat(command.execute(runChainContext)).isEqualTo(Command.CONTINUE_PROCESSING);

        addCommonVerifications();
        verifyNoInteractions(removeContainerCmd);
    }

    private void addCommonStubs(final boolean buildContainerExists) {
        final var containerId = randomAlphanumeric(16);

        final var detach = nextBoolean();
        final var cacheVolumeName = randomAlphanumeric(16);
        final var user = randomAlphanumeric(16);
        final var containerCommand = List.of(randomAlphanumeric(16));

        when(runChainContext.buildContainerExists()).thenReturn(buildContainerExists);
        if (buildContainerExists) {
            when(dockerClientService.findContainerByName(BUILD_IMAGE_NAME)).thenReturn(Optional.of(container));
            when(args.rebuild()).thenReturn(true);
            when(container.getId()).thenReturn(containerId);
            when(dockerClient.removeContainerCmd(containerId)).thenReturn(removeContainerCmd);
        }

        when(args.detach()).thenReturn(detach);
        when(buildImageConfig.getUser()).thenReturn(user);
        when(buildImageConfig.getCommand()).thenReturn(containerCommand);

        when(buildImageConfig.getVolume()).thenReturn(cacheVolumeName);

        when(dockerClient.createContainerCmd(BUILD_IMAGE_NAME)).thenReturn(createContainerCmd);
        when(createContainerCmd.withName(BUILD_IMAGE_NAME)).thenReturn(createContainerCmd);
        when(createContainerCmd.withHostConfig(any(HostConfig.class))).thenReturn(createContainerCmd);
        when(createContainerCmd.withUser(user)).thenReturn(createContainerCmd);
        when(createContainerCmd.withAttachStdin(detach)).thenReturn(createContainerCmd);
        when(createContainerCmd.withAttachStdout(detach)).thenReturn(createContainerCmd);
        when(createContainerCmd.withAttachStderr(detach)).thenReturn(createContainerCmd);
        when(createContainerCmd.withCmd(containerCommand)).thenReturn(createContainerCmd);
        when(createContainerCmd.exec()).thenReturn(mock(CreateContainerResponse.class));
    }

    private void addCommonVerifications() {
        verify(createContainerCmd).withHostConfig(hostConfigCaptor.capture());
        assertThat(hostConfigCaptor.getValue().getBinds()).hasSize(1);
        assertThat(hostConfigCaptor.getValue().getBinds()[0].getVolume().getPath())
                .isEqualTo(RunChainContext.GRADLE_CACHE_PATH);
        verifyNoMoreInteractions(dockerClient);
    }
}
