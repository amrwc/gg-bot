package dev.amrw.bin.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * Docker configuration POJO.
 */
@Getter
@Setter
public class DockerConfig {

    private String baseDirPath;
    private String dockerfileGradlePath;
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
