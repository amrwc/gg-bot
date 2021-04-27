package dev.amrw.runner.chain.run.helper;

import com.github.dockerjava.api.model.Container;
import dev.amrw.runner.config.BuildImageConfig;
import dev.amrw.runner.config.DockerConfig;
import dev.amrw.runner.helper.DockerClientHelper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.apache.commons.lang.RandomStringUtils.randomAlphabetic;
import static org.apache.commons.lang.RandomStringUtils.randomAlphanumeric;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RunChainHelperTest {

    @Mock
    private DockerConfig dockerConfig;
    @Mock
    private DockerClientHelper dockerClientHelper;
    @InjectMocks
    private RunChainHelper helper;

    @Mock
    private BuildImageConfig buildImageConfig;

    @Test
    @DisplayName("Should have found build container's ID")
    void shouldHaveFoundBuildContainerId() {
        final var buildContainerName = randomAlphabetic(16);
        final var container = mock(Container.class);
        final var containerId = randomAlphanumeric(16);

        when(dockerConfig.getBuildImageConfig()).thenReturn(buildImageConfig);
        when(buildImageConfig.getName()).thenReturn(buildContainerName);
        when(dockerClientHelper.findContainersByName(buildContainerName)).thenReturn(List.of(container));
        when(container.getId()).thenReturn(containerId);

        assertThat(helper.findBuildContainerId()).isEqualTo(containerId);
    }
}
