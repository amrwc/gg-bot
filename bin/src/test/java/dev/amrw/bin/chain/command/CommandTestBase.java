package dev.amrw.bin.chain.command;

import com.github.dockerjava.api.DockerClient;
import dev.amrw.bin.chain.context.RunChainContext;
import dev.amrw.bin.config.Config;
import dev.amrw.bin.config.DockerConfig;
import dev.amrw.bin.dto.RunArgs;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CommandTestBase {

    @Mock
    RunChainContext context;
    @Mock
    RunArgs args;
    @Mock
    Config config;
    @Mock
    DockerConfig dockerConfig;
    @Mock
    DockerClient dockerClient;

    void beforeEach() {
        when(context.getArgs()).thenReturn(args);
        when(context.getConfig()).thenReturn(config);
        when(config.getDockerConfig()).thenReturn(dockerConfig);
        when(context.getDockerClient()).thenReturn(dockerClient);
    }
}
