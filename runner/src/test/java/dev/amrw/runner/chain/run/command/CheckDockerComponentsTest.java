package dev.amrw.runner.chain.run.command;

import com.github.dockerjava.api.command.InspectVolumeResponse;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.Image;
import com.github.dockerjava.api.model.Network;
import dev.amrw.runner.config.BuildImageConfig;
import org.apache.commons.chain.Command;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CheckDockerComponentsTest extends RunChainCommandTestBase {

    @InjectMocks
    private CheckDockerComponents checkDockerComponents;

    @Mock
    private BuildImageConfig buildImageConfig;

    @BeforeEach
    void beforeEach() {
        super.beforeEach();
        when(dockerConfig.getBuildImageConfig()).thenReturn(buildImageConfig);
    }

    @Test
    @DisplayName("Should have checked Docker components and saved their state")
    void shouldHaveCheckedDockerComponents() {
        final var networkName = randomAlphabetic(16);
        final var buildCacheVolumeName = randomAlphabetic(16);
        final var buildImageName = randomAlphabetic(16);

        when(dockerConfig.getNetwork()).thenReturn(networkName);
        when(buildImageConfig.getVolume()).thenReturn(buildCacheVolumeName);
        when(buildImageConfig.getName()).thenReturn(buildImageName);

        final var networks = List.of(mock(Network.class));
        final var volumes = List.of(mock(InspectVolumeResponse.class));
        final var images = List.of(mock(Image.class));
        final var containers = List.of(mock(Container.class));

        when(dockerClientHelper.findNetworksByName(networkName)).thenReturn(networks);
        when(dockerClientHelper.findVolumesByName(buildCacheVolumeName)).thenReturn(volumes);
        when(dockerClientHelper.findImagesByName(buildImageName)).thenReturn(images);
        when(dockerClientHelper.findContainersByName(buildImageName)).thenReturn(containers);

        assertThat(checkDockerComponents.execute(runChainContext)).isEqualTo(Command.CONTINUE_PROCESSING);

        verify(runChainContext).networkExists(true);
        verify(runChainContext).buildCacheVolumeExists(true);
        verify(runChainContext).buildImageExists(true);
        verify(runChainContext).buildContainerExists(true);
    }
}
