package dev.amrw.runner.chain.command;

import com.github.dockerjava.api.DockerClient;
import dev.amrw.runner.helper.DockerClientHelper;
import dev.amrw.runner.chain.context.RunChainContext;
import dev.amrw.runner.config.Config;
import dev.amrw.runner.config.DockerConfig;
import dev.amrw.runner.dto.RunArgs;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RunChainCommandTestBase {

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
