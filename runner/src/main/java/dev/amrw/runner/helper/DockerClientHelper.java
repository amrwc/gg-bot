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
        log.debug("Finding networks by name (name={})", networkName);
        return dockerClient.listNetworksCmd()
                .withNameFilter(networkName)
                .exec();
    }

    /**
     * Finds volumes by the given name.
     * @param volumeName volume name to filter by
     * @return {@link List} of {@link InspectVolumeResponse} matching the given name
     */
    public List<InspectVolumeResponse> findVolumesByName(final String volumeName) {
        log.debug("Finding volumes by name (name={})", volumeName);
        return dockerClient
                .listVolumesCmd()
                .withFilter("name", List.of(volumeName))
                .exec()
                .getVolumes();
    }

    /**
     * Finds images by the given name.
     * <p>
     * NOTE: This method uses {@link Image#repoTags} to filter by name, because the
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
        log.debug("Finding images by name (name={})", imageName);
        return dockerClient.listImagesCmd().exec()
                .stream()
                .filter(image -> Arrays.stream(image.getRepoTags()).anyMatch(tag -> tag.startsWith(imageName)))
                .collect(Collectors.toList());
    }

    /**
     * Checks whether an image with the given name exists.
     * @param imageName Docker image's name.
     * @return whether the image exists
     */
    public boolean imageExists(final String imageName) {
        log.debug("Checking whether the image exists (name={})", imageName);
        final var imagesFiltered = findImagesByName(imageName);
        log.trace("Images filtered by name (name={}):\n{}", imageName, imagesFiltered);
        return !imagesFiltered.isEmpty();
    }

    /**
     * Finds containers with the given name.
     * <p>
     * NOTE: Docker doesn't allow duplicated names, therefore this list should contain at most one element.
     * @param containerName name of the container to find
     * @return {@link List} of {@link Container}s matching the given name
     */
    public List<Container> findContainersByName(final String containerName) {
        log.debug("Finding containers by name (name={})", containerName);
        return dockerClient.listContainersCmd()
                .withShowAll(true)
                .withFilter("name", Set.of(containerName))
                .exec();
    }

    /**
     * Checks whether a container with the given name exists.
     * @param containerName Docker container's name.
     * @return whether the container exists
     */
    public boolean containerExists(final String containerName) {
        log.debug("Checking whether the container exists (name={})", containerName);
        final var containers = findContainersByName(containerName);
        log.trace("Containers filtered by name (name={}):\n{}", containerName, containers);
        return !containers.isEmpty();
    }
}
