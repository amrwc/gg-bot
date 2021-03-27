package dev.amrw.bin.chain.command;

import com.github.dockerjava.api.DockerClient;
import dev.amrw.bin.DockerClientHelper;
import dev.amrw.bin.chain.context.RunChainContext;
import dev.amrw.bin.config.Config;
import dev.amrw.bin.config.DockerConfig;
import dev.amrw.bin.dto.RunArgs;
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
