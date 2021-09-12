package dev.amrw.runner.chain.run.command;

import com.github.dockerjava.api.command.InspectVolumeResponse;
import com.github.dockerjava.api.model.Network;
import dev.amrw.runner.chain.run.RunChainContext;
import org.apache.commons.chain.Command;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.apache.commons.lang3.RandomUtils.nextBoolean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CheckDockerComponentsTest extends RunChainCommandTestBase {

    private CheckDockerComponents command;

    @BeforeEach
    void beforeEach() {
        super.beforeEach();
        command = new CheckDockerComponents(dockerClientService);
    }

    @Test
    @DisplayName("Should have checked Docker components and saved their state")
    void shouldHaveCheckedDockerComponents() {
        final var buildImageExists = nextBoolean();
        final var buildContainerExists = nextBoolean();
        final var mainImageExists = nextBoolean();
        final var mainContainerExists = nextBoolean();

        dockerConfig.setNetwork(NETWORK_NAME);
        buildImageConfig.setVolume(BUILD_CACHE_VOLUME_NAME);

        final var networks = List.of(mock(Network.class));
        final var volumes = List.of(mock(InspectVolumeResponse.class));

        when(dockerClientService.findNetworksByName(NETWORK_NAME)).thenReturn(networks);
        when(dockerClientService.findVolumesByName(BUILD_CACHE_VOLUME_NAME)).thenReturn(volumes);
        when(dockerClientService.imageExists(BUILD_IMAGE_NAME)).thenReturn(buildImageExists);
        when(dockerClientService.containerExists(BUILD_IMAGE_NAME)).thenReturn(buildContainerExists);
        when(dockerClientService.imageExists(MAIN_IMAGE_NAME)).thenReturn(mainImageExists);
        when(dockerClientService.containerExists(MAIN_IMAGE_NAME)).thenReturn(mainContainerExists);

        final var runChainContext = new RunChainContext(config);
        assertThat(command.execute(runChainContext)).isEqualTo(Command.CONTINUE_PROCESSING);

        assertThat(runChainContext.networkExists()).isTrue();
        assertThat(runChainContext.buildCacheVolumeExists()).isTrue();
        assertThat(runChainContext.buildImageExists()).isEqualTo(buildImageExists);
        assertThat(runChainContext.buildContainerExists()).isEqualTo(buildContainerExists);
        assertThat(runChainContext.mainImageExists()).isEqualTo(mainImageExists);
        assertThat(runChainContext.mainContainerExists()).isEqualTo(mainContainerExists);
    }
}
