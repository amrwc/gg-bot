package dev.amrw.runner.chain.run.command;

import com.github.dockerjava.api.DockerClient;
import dev.amrw.runner.config.*;
import dev.amrw.runner.service.DockerClientService;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
public class RunChainCommandTestBase {

    static final String BUILD_IMAGE_NAME = "build-image-name";
    static final String BUILD_CONTAINER_ID = "build-container-id";
    static final String MAIN_IMAGE_NAME = "main-image-name";
    static final String MAIN_CONTAINER_ID = "main-container-id";
    static final String NETWORK_NAME = "network-name";
    static final String NETWORK_ID = "network-id";
    static final String VOLUME_NAME = "volume-name";
    static final String BUILD_CACHE_VOLUME_NAME = "build-cache-volume-name";

    MainImageConfig mainImageConfig;
    BuildImageConfig buildImageConfig;
    DatabaseContainerConfig databaseContainerConfig;
    DatabaseTestContainerConfig databaseTestContainerConfig;
    PgAdminContainerConfig pgAdminContainerConfig;
    DockerConfig dockerConfig;
    Config config;

    @Mock
    DockerClientService dockerClientService;
    @Mock
    DockerClient dockerClient;

    void beforeEach() {
        buildImageConfig = new BuildImageConfig();
        buildImageConfig.setName(BUILD_IMAGE_NAME);
        mainImageConfig = new MainImageConfig();
        mainImageConfig.setName(MAIN_IMAGE_NAME);
        databaseContainerConfig = new DatabaseContainerConfig();

        dockerConfig = new DockerConfig();
        dockerConfig.setBuildImageConfig(buildImageConfig);
        dockerConfig.setMainImageConfig(mainImageConfig);
        dockerConfig.setDatabaseContainerConfig(databaseContainerConfig);
        dockerConfig.setDatabaseTestContainer(databaseTestContainerConfig);
        dockerConfig.setPgAdminContainer(pgAdminContainerConfig);

        config = new Config();
        config.setDockerConfig(dockerConfig);

        lenient().when(dockerClientService.getDockerClient()).thenReturn(dockerClient);
    }
}
