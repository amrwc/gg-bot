package dev.amrw.bin.chain.command;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.*;
import com.github.dockerjava.api.model.Network;
import dev.amrw.bin.chain.context.RunChainContext;
import dev.amrw.bin.config.BuildImageConfig;
import dev.amrw.bin.config.Config;
import dev.amrw.bin.config.DockerConfig;
import org.apache.commons.chain.Command;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PrepareDockerEnvironmentTest {

    @InjectMocks
    private PrepareDockerEnvironment command;

    @Mock
    private RunChainContext context;
    @Mock
    private DockerClient dockerClient;
    @Mock
    private Config config;
    @Mock
    private DockerConfig dockerConfig;
    @Mock
    private BuildImageConfig buildImageConfig;

    @Mock
    private ListNetworksCmd listNetworksCmd;
    @Mock
    private CreateNetworkCmd createNetworkCmd;

    @Mock
    private ListVolumesCmd listVolumesCmd;
    @Mock
    private ListVolumesResponse listVolumesResponse;
    @Mock
    private CreateVolumeCmd createVolumeCmd;

    @ParameterizedTest
    @CsvSource({
            "false, false",
            "true, false",
            "false, true",
            "true, true",
    })
    @DisplayName("Should have prepared Docker environment")
    void shouldHavePreparedDockerEnvironment(final boolean networkExists, final boolean volumeExists) {
        when(context.getDockerClient()).thenReturn(dockerClient);
        when(context.getConfig()).thenReturn(config);
        when(config.getDockerConfig()).thenReturn(dockerConfig);
        addCreateNetworkStubs(networkExists);
        addCreateVolumeStubs(volumeExists);

        assertThat(command.execute(context)).isEqualTo(Command.CONTINUE_PROCESSING);

        verify(createNetworkCmd, times(networkExists ? 0 : 1)).exec();
        verify(createVolumeCmd, times(volumeExists ? 0 : 1)).exec();
    }

    private void addCreateNetworkStubs(final boolean networkExists) {
        final var networkName = randomAlphabetic(16);

        when(dockerConfig.getNetwork()).thenReturn(networkName);
        when(dockerClient.listNetworksCmd()).thenReturn(listNetworksCmd);
        when(listNetworksCmd.withNameFilter(networkName)).thenReturn(listNetworksCmd);
        when(listNetworksCmd.exec()).thenReturn(networkExists ? List.of(mock(Network.class)) : List.of());

        if (!networkExists) {
            when(dockerClient.createNetworkCmd()).thenReturn(createNetworkCmd);
            when(createNetworkCmd.withName(networkName)).thenReturn(createNetworkCmd);
        }
    }

    private void addCreateVolumeStubs(final boolean volumeExists) {
        final var volumeName = randomAlphabetic(16);

        when(dockerConfig.getBuildImageConfig()).thenReturn(buildImageConfig);
        when(buildImageConfig.getVolume()).thenReturn(volumeName);
        when(dockerClient.listVolumesCmd()).thenReturn(listVolumesCmd);
        when(listVolumesCmd.withFilter("name", List.of(volumeName))).thenReturn(listVolumesCmd);
        when(listVolumesCmd.exec()).thenReturn(listVolumesResponse);
        when(listVolumesResponse.getVolumes()).thenReturn(
                volumeExists ? List.of(mock(InspectVolumeResponse.class)) : List.of());

        if (!volumeExists) {
            when(dockerClient.createVolumeCmd()).thenReturn(createVolumeCmd);
            when(createVolumeCmd.withName(volumeName)).thenReturn(createVolumeCmd);
        }
    }
}
