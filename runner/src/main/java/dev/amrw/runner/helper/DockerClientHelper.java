package dev.amrw.runner.helper;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.InspectVolumeResponse;
import com.github.dockerjava.api.command.ListImagesCmd;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.Image;
import com.github.dockerjava.api.model.Network;
import lombok.extern.log4j.Log4j2;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Helper class for abstracting away common tasks.
 */
@Log4j2
public class DockerClientHelper {

    private final DockerClient dockerClient;

    public DockerClientHelper(final DockerClient dockerClient) {
        this.dockerClient = dockerClient;
    }

    /**
     * Finds networks by the given name.
     * @param networkName network name to filter by
     * @return {@link List} of {@link Network} matching the given name
     */
    public List<Network> findNetworksByName(final String networkName) {
        final var networks = dockerClient.listNetworksCmd()
                .withNameFilter(networkName)
                .exec();
        log.trace("Networks filtered by name (name={}):\n{}", networkName, networks);
        return networks;
    }

    /**
     * Finds volumes by the given name.
     * @param volumeName volume name to filter by
     * @return {@link List} of {@link InspectVolumeResponse} matching the given name
     */
    public List<InspectVolumeResponse> findVolumesByName(final String volumeName) {
        final var volumes = dockerClient
                .listVolumesCmd()
                .withFilter("name", List.of(volumeName))
                .exec()
                .getVolumes();
        log.trace("Volumes filtered by name (name={}):\n{}", volumeName, volumes);
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
     * @return {@link List} of {@link Image}s matching the given name
     */
    public List<Image> findImagesByName(final String imageName) {
        return dockerClient.listImagesCmd().exec()
                .stream()
                .filter(image -> image.getRepoTags() != null
                        && Arrays.stream(image.getRepoTags()).anyMatch(tag -> tag.startsWith(imageName)))
                .collect(Collectors.toList());
    }

    /**
     * Checks whether an image with the given name exists.
     * @param imageName Docker image's name.
     * @return whether the image exists
     */
    public boolean imageExists(final String imageName) {
        final var imagesFiltered = findImagesByName(imageName);
        log.trace("Images filtered by name (name={}):\n{}", imageName, imagesFiltered);
        return !imagesFiltered.isEmpty();
    }

    /**
     * Finds containers matching the given name part.
     * @param containerNameSlug part of the name to search by
     * @return {@link List} of {@link Container}s matching the given slug
     */
    public List<Container> findContainersByName(final String containerNameSlug) {
        return dockerClient.listContainersCmd()
                .withShowAll(true)
                .withFilter("name", Set.of(containerNameSlug))
                .exec();
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
     * @param containerName Docker container's name
     * @return whether the container exists
     */
    public boolean containerExists(final String containerName) {
        final var optionalContainer = findContainerByName(containerName);
        return optionalContainer.isPresent();
    }
}
