package dev.amrw.runner.service;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.InspectVolumeResponse;
import com.github.dockerjava.api.command.ListImagesCmd;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.Image;
import com.github.dockerjava.api.model.Network;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

// TODO: All these methods probably suffer from the same issue the `findContainersByName()` did -- finding by partial
//  name. Will need to filter them in Java streams too, checking String equality, just to be sure.

@Log4j2
public class DockerClientService {

    private static DockerClientService INSTANCE;

    @Getter
    private final DockerClient dockerClient;

    DockerClientService(final DockerClient dockerClient) {
        this.dockerClient = dockerClient;
    }

    private DockerClientService() {
        this.dockerClient = buildDefaultDockerClient();
    }

    public static DockerClientService getInstance() {
        return INSTANCE == null
                ? (INSTANCE = new DockerClientService())
                : INSTANCE;
    }

    /**
     * Finds networks matching the given name part.
     * @param networkNameSlug part of the network name to search by
     * @return {@link List} of {@link Network} matching the given slug.
     */
    public List<Network> findNetworksByName(final String networkNameSlug) {
        final var networks = dockerClient.listNetworksCmd()
                .withNameFilter(networkNameSlug)
                .exec();
        log.trace("Networks filtered by name [name={}]:\n{}", networkNameSlug, networks);
        return networks;
    }

    /**
     * Finds a network with the given name.
     * @param networkName network name
     * @return {@link Optional} {@link Network} if one has been found, empty {@link Optional} otherwise.
     */
    public Optional<Network> findNetworkByName(final String networkName) {
        return findNetworksByName(networkName)
                .stream()
                .filter(network -> network.getName().equals(networkName))
                .findFirst();
    }

    /**
     * Finds network ID by the given name.
     * @param networkName name of the network to find the ID of
     * @return ID of the {@link Network}, or empty string if not found.
     */
    public String findNetworkIdByName(final String networkName) {
        return findNetworkByName(networkName)
                .map(Network::getId)
                .orElse("");
    }

    /**
     * Finds volumes by the given name.
     * @param volumeName volume name to filter by
     * @return {@link List} of {@link InspectVolumeResponse} matching the given name.
     */
    public List<InspectVolumeResponse> findVolumesByName(final String volumeName) {
        final var volumes = dockerClient
                .listVolumesCmd()
                .withFilter("name", List.of(volumeName))
                .exec()
                .getVolumes();
        log.trace("Volumes filtered by name [name={}]:\n{}", volumeName, volumes);
        return volumes;
    }

    /**
     * Finds images by the given name.
     * <p>
     * NOTE: This method uses {@link Image#getRepoTags} to filter by name, because the
     * {@link ListImagesCmd#withImageNameFilter(String)} method doesn't actually filter the images in an intuitive way.
     * <p>
     * <h2>Example repo tags</h2>
     * <pre>
     *     [ggbot-gradle-build:latest],
     *     [<none>:<none>],
     *     [postgres:latest],
     *     [dpage/pgadmin4:latest]
     * </pre>
     * @param imageName image name to filter by
     * @return {@link List} of {@link Image}s matching the given name.
     */
    public List<Image> findImagesByName(final String imageName) {
        final var images = dockerClient.listImagesCmd().exec()
                .stream()
                .filter(image -> image.getRepoTags() != null
                        && Arrays.stream(image.getRepoTags()).anyMatch(tag -> tag.startsWith(imageName)))
                .collect(Collectors.toList());
        log.trace("Images filtered by name [name={}]:\n{}", imageName, images);
        return images;
    }

    /**
     * Checks whether an image with the given name exists.
     * @param imageName image name
     * @return whether the image exists.
     */
    public boolean imageExists(final String imageName) {
        final var imagesFiltered = findImagesByName(imageName);
        return !imagesFiltered.isEmpty();
    }

    /**
     * Finds containers matching the given name part.
     * @param containerNameSlug part of the container name to search by
     * @return {@link List} of {@link Container}s matching the given slug.
     */
    public List<Container> findContainersByName(final String containerNameSlug) {
        final var containers = dockerClient.listContainersCmd()
                .withShowAll(true)
                .withFilter("name", Set.of(containerNameSlug))
                .exec();
        log.trace("Containers filtered by name slug [nameSlug={}]\n{}", containerNameSlug, containers);
        return containers;
    }

    /**
     * Finds a container with the given name.
     * @param containerName name of the container
     * @return {@link Optional} {@link Container} if one has been found, empty {@link Optional} otherwise.
     */
    public Optional<Container> findContainerByName(final String containerName) {
        return findContainersByName(containerName)
                .stream()
                // The `names` field is an array, and usually only contains one element.
                .filter(container -> Arrays.stream(container.getNames())
                        // Container names start with a forward slash.
                        .anyMatch(name -> name.substring(1).equals(containerName)))
                .findFirst();
    }

    /**
     * Finds a container ID by the given container name.
     * @param containerName name of the container
     * @return ID of the container, or empty string if not found.
     */
    public String findContainerIdByName(final String containerName) {
        return findContainerByName(containerName)
                .map(Container::getId)
                .orElse("");
    }

    /**
     * Checks whether a container with the given name exists.
     * @param containerName name of the container
     * @return whether the container exists.
     */
    public boolean containerExists(final String containerName) {
        final var optionalContainer = findContainerByName(containerName);
        return optionalContainer.isPresent();
    }

    private static DockerClient buildDefaultDockerClient() {
        final var dockerClientConfig = DefaultDockerClientConfig
                .createDefaultConfigBuilder()
                .build();
        final var httpClient = new ApacheDockerHttpClient.Builder()
                .dockerHost(dockerClientConfig.getDockerHost())
                .sslConfig(dockerClientConfig.getSSLConfig())
                .build();
        return DockerClientImpl.getInstance(dockerClientConfig, httpClient);
    }
}
