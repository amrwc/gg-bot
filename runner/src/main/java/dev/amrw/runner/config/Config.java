package dev.amrw.runner.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Configuration POJO.
 */
@Data
public class Config {

    @JsonProperty("docker")
    private DockerConfig dockerConfig;
}
