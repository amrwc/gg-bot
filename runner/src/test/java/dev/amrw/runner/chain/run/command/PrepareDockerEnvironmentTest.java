package dev.amrw.runner.chain.run.command;

import com.github.dockerjava.api.command.CreateNetworkCmd;
import com.github.dockerjava.api.command.CreateVolumeCmd;
import dev.amrw.runner.config.BuildImageConfig;
import org.apache.commons.chain.Command;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PrepareDockerEnvironmentTest extends RunChainCommandTestBase {

    private PrepareDockerEnvironment command;

    @Mock
    private BuildImageConfig buildImageConfig;
    @Mock
    private CreateNetworkCmd createNetworkCmd;
    @Mock
    private CreateVolumeCmd createVolumeCmd;

    @BeforeEach
    void beforeEach() {
        super.beforeEach();

        command = new PrepareDockerEnvironment();
    }

    @ParameterizedTest
    @CsvSource({
            "false, false",
            "true, false",
            "false, true",
            "true, true",
    })
    @DisplayName("Should have prepared Docker environment")
    void shouldHavePreparedDockerEnvironment(final boolean networkExists, final boolean volumeExists) {
        addCreateNetworkStubs(networkExists);
        addCreateVolumeStubs(volumeExists);

        assertThat(command.execute(runChainContext)).isEqualTo(Command.CONTINUE_PROCESSING);

        verify(createNetworkCmd, times(networkExists ? 0 : 1)).exec();
        verify(createVolumeCmd, times(volumeExists ? 0 : 1)).exec();
    }

    private void addCreateNetworkStubs(final boolean networkExists) {
        when(dockerConfig.getNetwork()).thenReturn(NETWORK_NAME);
        when(runChainContext.networkExists()).thenReturn(networkExists);

        if (!networkExists) {
            when(dockerClient.createNetworkCmd()).thenReturn(createNetworkCmd);
            when(createNetworkCmd.withName(NETWORK_NAME)).thenReturn(createNetworkCmd);
        }
    }

    private void addCreateVolumeStubs(final boolean volumeExists) {
        when(dockerConfig.getBuildImageConfig()).thenReturn(buildImageConfig);
        when(buildImageConfig.getVolume()).thenReturn(VOLUME_NAME);
        when(runChainContext.buildCacheVolumeExists()).thenReturn(volumeExists);

        if (!volumeExists) {
            when(dockerClient.createVolumeCmd()).thenReturn(createVolumeCmd);
            when(createVolumeCmd.withName(VOLUME_NAME)).thenReturn(createVolumeCmd);
        }
    }
}
