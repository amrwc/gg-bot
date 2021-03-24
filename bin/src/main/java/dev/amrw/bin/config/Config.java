package dev.amrw.bin.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * Configuration POJO.
 */
@Getter
@Setter
public class Config {

    @JsonProperty("spring")
    private SpringConfig springConfig;
    @JsonProperty("database")
    private DatabaseConfig databaseConfig;
    @JsonProperty("docker")
    private DockerConfig dockerConfig;
}
