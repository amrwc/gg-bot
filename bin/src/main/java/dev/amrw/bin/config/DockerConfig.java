package dev.amrw.bin.config;

import lombok.Getter;
import lombok.Setter;

/**
 * Docker configuration POJO.
 */
@Getter
@Setter
public class DockerConfig {

    /** Docker base directory path, relative to this Gradle subproject. */
    public static final String BASE_DIR_PATH = "..";
    /** Dockerfile directory path, relative to this Gradle subproject. */
    public static final String DOCKERFILE_GRADLE_PATH = "../docker/Dockerfile-gradle";

    private String cacheVolume;
    private String network;
    private String testNetwork;
    private String mainImage;
    private String buildImage;
    private String buildCommand;
}
