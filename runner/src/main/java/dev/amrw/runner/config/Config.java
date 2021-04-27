package dev.amrw.runner.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * Configuration POJO.
 */
@Getter
@Setter
public class Config {

    @JsonProperty("docker")
    private DockerConfig dockerConfig;
}