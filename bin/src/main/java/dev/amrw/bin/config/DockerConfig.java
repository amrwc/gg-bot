package dev.amrw.bin.config;

import lombok.Getter;
import lombok.Setter;

/**
 * Docker configuration POJO.
 */
@Getter
@Setter
public class DockerConfig {

    private String cacheVolume;
    private String network;
    private String testNetwork;
    private String mainImage;
    private String buildImage;
    private String buildCommand;
}
