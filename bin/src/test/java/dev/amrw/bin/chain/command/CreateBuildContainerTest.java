package dev.amrw.bin.chain.command;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.ListContainersCmd;
import com.github.dockerjava.api.model.Container;
import dev.amrw.bin.chain.context.RunChainContext;
import dev.amrw.bin.config.BuildImageConfig;
import dev.amrw.bin.config.Config;
import dev.amrw.bin.config.DockerConfig;
import dev.amrw.bin.dto.RunArgs;
import org.apache.commons.chain.Command;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateBuildContainerTest {

    @InjectMocks
    private CreateBuildContainer command;

    @Mock
    private RunChainContext context;
    @Mock
    private RunArgs args;
    @Mock
    private Config config;
    @Mock
    private DockerConfig dockerConfig;
    @Mock
    private BuildImageConfig buildImageConfig;

    @Mock
    private DockerClient dockerClient;
    @Mock
    private ListContainersCmd listContainersCmd;

    private String buildImageName;

    @BeforeEach
    void beforeEach() {
        buildImageName = randomAlphabetic(16);

        when(context.getArgs()).thenReturn(args);
        when(context.getConfig()).thenReturn(config);
        when(config.getDockerConfig()).thenReturn(dockerConfig);
        when(dockerConfig.getBuildImageConfig()).thenReturn(buildImageConfig);
        when(buildImageConfig.getName()).thenReturn(buildImageName);

        when(context.getDockerClient()).thenReturn(dockerClient);
        when(dockerClient.listContainersCmd()).thenReturn(listContainersCmd);
        when(listContainersCmd.withShowAll(true)).thenReturn(listContainersCmd);
        when(listContainersCmd.withFilter("name", Set.of(buildImageName))).thenReturn(listContainersCmd);
    }

    @Test
    @DisplayName("Should have skipped creating the build container if it already exists")
    void shouldHaveSkippedWhenContainerExists() {
        final var container = mock(Container.class);
        when(listContainersCmd.exec()).thenReturn(List.of(container));
        when(args.rebuild()).thenReturn(false);

        assertThat(command.execute(context)).isEqualTo(Command.PROCESSING_COMPLETE);
    }
}
