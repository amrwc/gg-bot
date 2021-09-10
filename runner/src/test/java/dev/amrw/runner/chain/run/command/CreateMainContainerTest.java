package dev.amrw.runner.chain.run.command;

import com.github.dockerjava.api.command.ConnectToNetworkCmd;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.RemoveContainerCmd;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.ExposedPort;
import dev.amrw.runner.config.MainImageConfig;
import dev.amrw.runner.exception.InvalidEnvarException;
import dev.amrw.runner.service.EnvarsService;
import org.apache.commons.chain.Command;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.apache.commons.lang3.RandomUtils.nextBoolean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateMainContainerTest extends RunChainCommandTestBase {

    private static final String MAIN_IMAGE_NAME = "main-image-name";
    private static final String MAIN_CONTAINER_ID = "65acd60b586f";
    private static final String NETWORK_NAME = "network-name";
    private static final String NETWORK_ID = "081692bae9cb";

    private MainImageConfig mainImageConfig;

    @Mock
    private EnvarsService envarsService;

    private CreateMainContainer command;

    @Mock
    private RemoveContainerCmd removeContainerCmd;
    @Mock
    private CreateContainerCmd createContainerCmd;
    @Mock
    private CreateContainerResponse createContainerResponse;
    @Mock
    private ConnectToNetworkCmd connectToNetworkCmd;

    @BeforeEach
    void beforeEach() {
        super.beforeEach();

        command = new CreateMainContainer(envarsService);

        mainImageConfig = new MainImageConfig();
        mainImageConfig.setName(MAIN_IMAGE_NAME);
        mainImageConfig.setPort(8080);
        mainImageConfig.setDebugPort(8000);
    }

    @Test
    @DisplayName("Should have skipped creating the main container if it already exists")
    void shouldHaveSkippedWhenContainerExists() {
        when(runChainContext.mainContainerExists()).thenReturn(true);
        when(args.rebuild()).thenReturn(false);

        assertThat(command.execute(runChainContext)).isEqualTo(Command.CONTINUE_PROCESSING);

        verifyNoInteractions(dockerClient);
    }

    @Test
    @DisplayName("Should have thrown an exception when the envars were invalid")
    void shouldHaveThrownWhenEnvarsWereInvalid() {
        when(runChainContext.mainContainerExists()).thenReturn(false);
        when(envarsService.verifyEnvars(CreateMainContainer.REQUIRED_ENVARS)).thenReturn(false);

        assertThatThrownBy(() -> command.execute(runChainContext))
                .isInstanceOf(InvalidEnvarException.class)
                .hasMessageContaining("Missing or wrong values of one or more required envars");

        verifyNoInteractions(dockerClient);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    @DisplayName("Should have created the main container")
    void shouldHaveCreatedContainer(final boolean mainContainerExists) {
        final Map<String, String> env = CreateMainContainer.REQUIRED_ENVARS.stream()
                .collect(Collectors.toMap(Function.identity(), envarName -> randomAlphanumeric(8)));

        when(runChainContext.getMainImageName()).thenReturn(MAIN_IMAGE_NAME);

        when(runChainContext.mainContainerExists()).thenReturn(true);
        when(args.rebuild()).thenReturn(true);

        when(envarsService.verifyEnvars(CreateMainContainer.REQUIRED_ENVARS)).thenReturn(true);
        if (mainContainerExists) {
            addRemoveContainerCmdStubbings();
        }

        when(envarsService.getEnv()).thenReturn(env);

        addCreateContainerCmdStubbings();
        addConnectToNetworkCmdStubbings();

        assertThat(command.execute(runChainContext)).isEqualTo(Command.CONTINUE_PROCESSING);

        if (mainContainerExists) {
            verify(removeContainerCmd).exec();
        }
        verify(connectToNetworkCmd).exec();
    }

    private void addRemoveContainerCmdStubbings() {
        final var container = mock(Container.class);
        final var oldContainerId = "c5ece54ab3af";
        final var oldContainerName = "old-container-name";
        when(dockerClientHelper.findContainerByName(MAIN_IMAGE_NAME)).thenReturn(Optional.of(container));
        when(container.getId()).thenReturn(oldContainerId);
        when(container.getNames()).thenReturn(new String[] {oldContainerName, oldContainerName});
        when(dockerClient.removeContainerCmd(oldContainerId)).thenReturn(removeContainerCmd);
    }

    private void addCreateContainerCmdStubbings() {
        final var detach = nextBoolean();

        when(args.detach()).thenReturn(detach);
        when(dockerConfig.getMainImageConfig()).thenReturn(mainImageConfig);
        when(args.debug()).thenReturn(true);

        when(dockerClient.createContainerCmd(MAIN_IMAGE_NAME)).thenReturn(createContainerCmd);
        when(createContainerCmd.withName(MAIN_IMAGE_NAME)).thenReturn(createContainerCmd);
        when(createContainerCmd.withAttachStdin(!detach)).thenReturn(createContainerCmd);
        when(createContainerCmd.withAttachStdout(!detach)).thenReturn(createContainerCmd);
        when(createContainerCmd.withAttachStderr(!detach)).thenReturn(createContainerCmd);
        when(createContainerCmd.withExposedPorts(ArgumentMatchers.<List<ExposedPort>>any()))
                .thenReturn(createContainerCmd);
        when(createContainerCmd.withEnv(ArgumentMatchers.<List<String>>any())).thenReturn(createContainerCmd);
        when(createContainerCmd.exec()).thenReturn(createContainerResponse);
    }

    private void addConnectToNetworkCmdStubbings() {
        when(dockerClientHelper.findContainerIdByName(MAIN_IMAGE_NAME)).thenReturn(MAIN_CONTAINER_ID);

        when(dockerConfig.getNetwork()).thenReturn(NETWORK_NAME);
        when(dockerClientHelper.findNetworkIdByName(NETWORK_NAME)).thenReturn(NETWORK_ID);

        when(dockerClient.connectToNetworkCmd()).thenReturn(connectToNetworkCmd);
        when(connectToNetworkCmd.withNetworkId(NETWORK_ID)).thenReturn(connectToNetworkCmd);
        when(connectToNetworkCmd.withContainerId(MAIN_CONTAINER_ID)).thenReturn(connectToNetworkCmd);
        when(createContainerResponse.getId()).thenReturn(MAIN_CONTAINER_ID);
    }
}
