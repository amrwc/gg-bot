package dev.amrw.bin.chain.command;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.*;
import com.github.dockerjava.api.model.Network;
import dev.amrw.bin.chain.context.RunChainContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

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
        addCreateNetworkStubbings(networkExists);
        addCreateVolumeStubbings(volumeExists);

        assertThat(command.execute(context)).isTrue();

        verify(createNetworkCmd, times(networkExists ? 0 : 1)).exec();
        verify(createVolumeCmd, times(volumeExists ? 0 : 1)).exec();
    }

    private void addCreateNetworkStubbings(final boolean networkExists) {
        when(dockerClient.listNetworksCmd()).thenReturn(listNetworksCmd);
        when(listNetworksCmd.withNameFilter("ggbot-network")).thenReturn(listNetworksCmd);
        when(listNetworksCmd.exec()).thenReturn(networkExists ? List.of(mock(Network.class)) : List.of());

        if (!networkExists) {
            when(dockerClient.createNetworkCmd()).thenReturn(createNetworkCmd);
            when(createNetworkCmd.withName("ggbot-network")).thenReturn(createNetworkCmd);
        }
    }

    private void addCreateVolumeStubbings(final boolean volumeExists) {
        when(dockerClient.listVolumesCmd()).thenReturn(listVolumesCmd);
        when(listVolumesCmd.withFilter("name", List.of("gradle-build-cache"))).thenReturn(listVolumesCmd);
        when(listVolumesCmd.exec()).thenReturn(listVolumesResponse);
        when(listVolumesResponse.getVolumes()).thenReturn(
                volumeExists ? List.of(mock(InspectVolumeResponse.class)) : List.of());

        if (!volumeExists) {
            when(dockerClient.createVolumeCmd()).thenReturn(createVolumeCmd);
            when(createVolumeCmd.withName("gradle-build-cache")).thenReturn(createVolumeCmd);
        }
    }
}
