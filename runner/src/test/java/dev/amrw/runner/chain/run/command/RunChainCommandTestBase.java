package dev.amrw.runner.chain.run.command;

import com.github.dockerjava.api.DockerClient;
import dev.amrw.runner.chain.run.RunChainContext;
import dev.amrw.runner.config.Config;
import dev.amrw.runner.config.DockerConfig;
import dev.amrw.runner.dto.RunArgs;
import dev.amrw.runner.helper.DockerClientHelper;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RunChainCommandTestBase {

    static final String BUILD_IMAGE_NAME = "build-image-name";
    static final String BUILD_CONTAINER_ID = "98kjn98b645n";
    static final String MAIN_IMAGE_NAME = "main-image-name";
    static final String MAIN_CONTAINER_ID = "65acd60b586f";
    static final String NETWORK_NAME = "network-name";
    static final String NETWORK_ID = "081692bae9cb";
    static final String VOLUME_NAME = "volume-name";

    // TODO: Reduce mocking here
    @Mock
    RunChainContext runChainContext;
    @Mock
    RunArgs args;
    @Mock
    Config config;
    @Mock
    DockerConfig dockerConfig;
    @Mock
    DockerClient dockerClient;
    @Mock
    DockerClientHelper dockerClientHelper;

    void beforeEach() {
        when(runChainContext.getArgs()).thenReturn(args);
        when(runChainContext.getConfig()).thenReturn(config);
        when(config.getDockerConfig()).thenReturn(dockerConfig);
        when(runChainContext.getDockerClient()).thenReturn(dockerClient);
        when(runChainContext.getDockerClientHelper()).thenReturn(dockerClientHelper);
    }
}
