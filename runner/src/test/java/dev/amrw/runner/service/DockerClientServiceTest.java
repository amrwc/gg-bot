package dev.amrw.runner.service;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.*;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.Image;
import com.github.dockerjava.api.model.Network;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DockerClientServiceTest {

    private static final String NETWORK_NAME = "network-name";
    private static final String CONTAINER_NAME = "container-name";

    @Mock
    private DockerClient dockerClient;

    private DockerClientService service;

    @Mock
    private ListNetworksCmd listNetworksCmd;
    @Mock
    private ListContainersCmd listContainersCmd;
    @Mock
    private Container container1;
    @Mock
    private Container container2;

    @BeforeEach
    void beforeEach() {
        service = new DockerClientService(dockerClient);
    }

    @Test
    @DisplayName("Should have found Docker networks by name")
    void shouldHaveFoundNetworksByName() {
        final var networks = List.of(new Network());

        when(dockerClient.listNetworksCmd()).thenReturn(listNetworksCmd);
        when(listNetworksCmd.withNameFilter(NETWORK_NAME)).thenReturn(listNetworksCmd);
        when(listNetworksCmd.exec()).thenReturn(networks);

        assertThat(service.findNetworksByName(NETWORK_NAME)).isEqualTo(networks);
    }

    @Test
    @DisplayName("Should have found network by name")
    void shouldHaveFoundNetworkByName() {
        final var network = mock(Network.class);
        final var networks = List.of(network);

        when(dockerClient.listNetworksCmd()).thenReturn(listNetworksCmd);
        when(listNetworksCmd.withNameFilter(NETWORK_NAME)).thenReturn(listNetworksCmd);
        when(listNetworksCmd.exec()).thenReturn(networks);
        when(network.getName()).thenReturn(NETWORK_NAME);

        final var result = service.findNetworkByName(NETWORK_NAME);
        assertThat(result).isNotEmpty();
        assertThat(result.get()).isEqualTo(network);
    }

    @ParameterizedTest
    @CsvSource({
            "network-name, 081692bae9cb21178880038cd1621bb1d49760fc85ae117d8cc54a02b299026b",
            "other-network-name, ''",
    })
    @DisplayName("Should have found network ID by name")
    void shouldHaveFoundNetworkIdByName(final String networkName, final String expectedResult) {
        final var network = mock(Network.class);
        final var networks = List.of(network);
        final var networkId = "081692bae9cb21178880038cd1621bb1d49760fc85ae117d8cc54a02b299026b";

        when(dockerClient.listNetworksCmd()).thenReturn(listNetworksCmd);
        when(listNetworksCmd.withNameFilter(NETWORK_NAME)).thenReturn(listNetworksCmd);
        when(listNetworksCmd.exec()).thenReturn(networks);
        when(network.getName()).thenReturn(networkName);
        lenient().when(network.getId()).thenReturn(networkId);

        assertThat(service.findNetworkIdByName(NETWORK_NAME)).isEqualTo(expectedResult);
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

        assertThat(service.findVolumesByName(volumeName)).isEqualTo(inspectVolumeResponses);
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

        assertThat(service.findImagesByName(imageName)).isEqualTo(List.of(goodImage));
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

        assertThat(service.imageExists(imageName)).isEqualTo(imageExists);
    }

    @Test
    @DisplayName("Should have found Docker containers by name")
    void shouldHaveFoundContainersByName() {
        findContainersByNameStubbings();
        assertThat(service.findContainersByName(CONTAINER_NAME)).isEqualTo(List.of(container1, container2));
    }

    @Test
    @DisplayName("Should have found a Docker container with the given name")
    void shouldHaveFoundContainerByName() {
        findContainersByNameStubbings();
        multipleContainersFoundStubbings(CONTAINER_NAME);
        assertThat(service.findContainerByName(CONTAINER_NAME)).isEqualTo(Optional.of(container2));
    }

    @Test
    void shouldHaveFoundContainerIdByName() {
        findContainersByNameStubbings();
        multipleContainersFoundStubbings(CONTAINER_NAME);

        final var container2Id = randomAlphanumeric(32);
        when(container2.getId()).thenReturn(container2Id);

        assertThat(service.findContainerIdByName(CONTAINER_NAME)).isEqualTo(container2Id);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    @DisplayName("Should have determined whether a Docker container with the given name exists")
    void shouldHaveDeterminedWhetherContainerExists(final boolean containerExists) {
        findContainersByNameStubbings();
        multipleContainersFoundStubbings(containerExists ? CONTAINER_NAME : randomAlphanumeric(16));
        assertThat(service.containerExists(CONTAINER_NAME)).isEqualTo(containerExists);
    }

    private void findContainersByNameStubbings() {
        when(dockerClient.listContainersCmd()).thenReturn(listContainersCmd);
        when(listContainersCmd.withShowAll(true)).thenReturn(listContainersCmd);
        when(listContainersCmd.withFilter("name", Set.of(CONTAINER_NAME))).thenReturn(listContainersCmd);
        when(listContainersCmd.exec()).thenReturn(List.of(container1, container2));
    }

    private void multipleContainersFoundStubbings(final String containerName) {
        when(container1.getNames()).thenReturn(new String[] {
                "/" + randomAlphanumeric(16),
                "/" + randomAlphanumeric(16)
        });
        when(container2.getNames()).thenReturn(new String[] {
                "/" + randomAlphanumeric(16),
                "/" + containerName
        });
    }
}
