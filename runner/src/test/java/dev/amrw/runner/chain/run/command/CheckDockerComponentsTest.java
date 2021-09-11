package dev.amrw.runner.chain.run.command;

import com.github.dockerjava.api.command.InspectVolumeResponse;
import com.github.dockerjava.api.model.Network;
import dev.amrw.runner.config.BuildImageConfig;
import dev.amrw.runner.config.MainImageConfig;
import org.apache.commons.chain.Command;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.apache.commons.lang3.RandomUtils.nextBoolean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CheckDockerComponentsTest extends RunChainCommandTestBase {

    private CheckDockerComponents command;

    @Mock
    private BuildImageConfig buildImageConfig;
    @Mock
    private MainImageConfig mainImageConfig;

    @BeforeEach
    void beforeEach() {
        super.beforeEach();

        command = new CheckDockerComponents(dockerClientService);

        when(dockerConfig.getBuildImageConfig()).thenReturn(buildImageConfig);
        when(dockerConfig.getMainImageConfig()).thenReturn(mainImageConfig);
    }

    @Test
    @DisplayName("Should have checked Docker components and saved their state")
    void shouldHaveCheckedDockerComponents() {
        final var networkName = randomAlphabetic(16);
        final var buildCacheVolumeName = randomAlphabetic(16);
        final var buildImageName = randomAlphabetic(16);
        final var buildImageExists = nextBoolean();
        final var mainImageName = randomAlphabetic(16);
        final var mainImageExists = nextBoolean();

        when(dockerConfig.getNetwork()).thenReturn(networkName);
        when(buildImageConfig.getVolume()).thenReturn(buildCacheVolumeName);
        when(buildImageConfig.getName()).thenReturn(buildImageName);
        when(mainImageConfig.getName()).thenReturn(mainImageName);

        final var networks = List.of(mock(Network.class));
        final var volumes = List.of(mock(InspectVolumeResponse.class));

        when(dockerClientService.findNetworksByName(networkName)).thenReturn(networks);
        when(dockerClientService.findVolumesByName(buildCacheVolumeName)).thenReturn(volumes);
        when(dockerClientService.imageExists(buildImageName)).thenReturn(buildImageExists);
        when(dockerClientService.containerExists(buildImageName)).thenReturn(buildImageExists);
        when(dockerClientService.imageExists(mainImageName)).thenReturn(mainImageExists);
        when(dockerClientService.containerExists(mainImageName)).thenReturn(mainImageExists);

        assertThat(command.execute(runChainContext)).isEqualTo(Command.CONTINUE_PROCESSING);

        verify(runChainContext).networkExists(true);
        verify(runChainContext).buildCacheVolumeExists(true);
        verify(runChainContext).buildImageExists(buildImageExists);
        verify(runChainContext).buildContainerExists(buildImageExists);
        verify(runChainContext).mainImageExists(mainImageExists);
        verify(runChainContext).mainContainerExists(mainImageExists);
    }
}
