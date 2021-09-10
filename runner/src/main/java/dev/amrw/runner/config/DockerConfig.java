package dev.amrw.runner.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Docker configuration POJO.
 */
@Data
public class DockerConfig {

    private String baseDirPath;
    private String dockerfileGradlePath;
    private String dockerfileMainPath;
    private String network;
    private String testNetwork;

    @JsonProperty("buildImage")
    private BuildImageConfig buildImageConfig;
    @JsonProperty("mainImage")
    private MainImageConfig mainImageConfig;
    @JsonProperty("databaseContainer")
    private DatabaseContainerConfig databaseContainerConfig;
    @JsonProperty("databaseTestContainer")
    private DatabaseTestContainerConfig databaseTestContainer;
    @JsonProperty("pgAdminContainer")
    private PgAdminContainerConfig pgAdminContainer;
}
