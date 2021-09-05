package dev.amrw.runner.helper;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.*;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.Image;
import com.github.dockerjava.api.model.Network;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DockerClientHelperTest {

    @Mock
    private DockerClient dockerClient;
    @InjectMocks
    private DockerClientHelper helper;

    @Test
    @DisplayName("Should have found Docker networks by name")
    void shouldHaveFoundNetworksByName() {
        final var networkName = randomAlphanumeric(16);
        final var listNetworksCmd = mock(ListNetworksCmd.class);
        final var networks = List.of(mock(Network.class));

        when(dockerClient.listNetworksCmd()).thenReturn(listNetworksCmd);
        when(listNetworksCmd.withNameFilter(networkName)).thenReturn(listNetworksCmd);
        when(listNetworksCmd.exec()).thenReturn(networks);

        assertThat(helper.findNetworksByName(networkName)).isEqualTo(networks);
    }

    @Test
    @DisplayName("Should have found Docker volumes by name")
    void shouldHaveFoundVolumesByName() {
        final var volumeName = randomAlphanumeric(16);
        final var listVolumesCmd = mock(ListVolumesCmd.class);
        final var listVolumesResponse = mock(ListVolumesResponse.class);
        final var inspectVolumeResponses = List.of(mock(InspectVolumeResponse.class));

        when(dockerClient.listVolumesCmd()).thenReturn(listVolumesCmd);
        when(listVolumesCmd.withFilter("name", List.of(volumeName))).thenReturn(listVolumesCmd);
        when(listVolumesCmd.exec()).thenReturn(listVolumesResponse);
        when(listVolumesResponse.getVolumes()).thenReturn(inspectVolumeResponses);

        assertThat(helper.findVolumesByName(volumeName)).isEqualTo(inspectVolumeResponses);
    }

    @Test
    @DisplayName("Should have found Docker volumes by name")
    void shouldHaveFoundImagesByName() {
        final var imageName = randomAlphanumeric(16);
        final var listImagesCmd = mock(ListImagesCmd.class);
        final var goodImage = mock(Image.class);
        final var badImage = mock(Image.class);
        final var worseImage = mock(Image.class);

        when(dockerClient.listImagesCmd()).thenReturn(listImagesCmd);
        when(listImagesCmd.exec()).thenReturn(List.of(badImage, goodImage, worseImage));
        when(badImage.getRepoTags()).thenReturn(new String[] {randomAlphanumeric(24)});
        when(worseImage.getRepoTags()).thenReturn(null);
        when(goodImage.getRepoTags()).thenReturn(new String[] {imageName + randomAlphanumeric(8)});

        assertThat(helper.findImagesByName(imageName)).isEqualTo(List.of(goodImage));
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    @DisplayName("Should have determined whether a Docker image with the given name exists")
    void shouldHaveDeterminedWhetherImageExists(final boolean imageExists) {
        final var imageName = randomAlphanumeric(16);
        final var listImagesCmd = mock(ListImagesCmd.class);
        final var goodImage = mock(Image.class);
        final var badImage = mock(Image.class);

        when(dockerClient.listImagesCmd()).thenReturn(listImagesCmd);
        when(listImagesCmd.exec()).thenReturn(List.of(badImage, goodImage));
        when(badImage.getRepoTags()).thenReturn(new String[] {randomAlphanumeric(24)});
        when(goodImage.getRepoTags())
                .thenReturn(new String[] {(imageExists ? imageName : "") + randomAlphanumeric(8)});

        assertThat(helper.imageExists(imageName)).isEqualTo(imageExists);
    }

    @Test
    @DisplayName("Should have found Docker containers by name")
    void shouldHaveFoundContainersByName() {
        final var containerName = randomAlphanumeric(16);
        final var listContainersCmd = mock(ListContainersCmd.class);
        final var container = mock(Container.class);

        when(dockerClient.listContainersCmd()).thenReturn(listContainersCmd);
        when(listContainersCmd.withShowAll(true)).thenReturn(listContainersCmd);
        when(listContainersCmd.withFilter("name", Set.of(containerName))).thenReturn(listContainersCmd);
        when(listContainersCmd.exec()).thenReturn(List.of(container));

        assertThat(helper.findContainersByName(containerName)).isEqualTo(List.of(container));
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    @DisplayName("Should have determined whether a Docker container with the given name exists")
    void shouldHaveDeterminedWhetherContainerExists(final boolean containerExists) {
        final var containerName = randomAlphanumeric(16);
        final var listContainersCmd = mock(ListContainersCmd.class);
        final var container = mock(Container.class);

        when(dockerClient.listContainersCmd()).thenReturn(listContainersCmd);
        when(listContainersCmd.withShowAll(true)).thenReturn(listContainersCmd);
        when(listContainersCmd.withFilter("name", Set.of(containerName))).thenReturn(listContainersCmd);
        when(listContainersCmd.exec()).thenReturn(containerExists ? List.of(container) : List.of());

        assertThat(helper.containerExists(containerName)).isEqualTo(containerExists);
    }
}
