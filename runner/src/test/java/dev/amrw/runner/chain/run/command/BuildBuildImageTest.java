package dev.amrw.runner.chain.run.command;

import com.github.dockerjava.api.command.BuildImageCmd;
import com.github.dockerjava.api.command.RemoveImageCmd;
import com.github.dockerjava.api.model.Image;
import dev.amrw.runner.callback.BuildImageResultCallback;
import dev.amrw.runner.chain.run.RunChainContext;
import dev.amrw.runner.dto.RunArgs;
import org.apache.commons.chain.Command;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BuildBuildImageTest extends RunChainCommandTestBase {

    private BuildBuildImage command;

    @Mock
    private Image image;

    @Mock
    private BuildImageCmd buildImageCmd;
    @Mock
    private RemoveImageCmd removeImageCmd;
    @Mock
    private BuildImageResultCallback buildImageResultCallback;
    @Captor
    private ArgumentCaptor<Set<String>> cacheFromCaptor;

    @BeforeEach
    void beforeEach() {
        super.beforeEach();
        command = new BuildBuildImage(dockerClientService);
    }

    @Test
    @DisplayName("Should have skipped building the build image if it already exists")
    void shouldHaveSkippedWhenImageExists() {
        final var args = RunArgs.builder().rebuild(false).build();
        final var runChainContext = new RunChainContext(config, args);
        runChainContext.buildImageExists(true);

        assertThat(command.execute(runChainContext)).isEqualTo(Command.CONTINUE_PROCESSING);
    }

    @ParameterizedTest
    @MethodSource
    @DisplayName("Should have built the build image")
    void shouldHaveBuiltBuildImage(final boolean noCache, final List<String> cacheFrom) {
        final var existingImageId = "existing-image-id";
        final var newImageId = "sha256:" + "new-image-id";

        final var argsBuilder = RunArgs.builder()
                .rebuild(true)
                .noCache(noCache);

        if (noCache) {
            when(dockerClientService.findImagesByName(BUILD_IMAGE_NAME)).thenReturn(List.of(image));
            when(image.getId()).thenReturn(existingImageId);
            when(dockerClient.removeImageCmd(existingImageId)).thenReturn(removeImageCmd);
        }

        dockerConfig.setBaseDirPath("base-dir-path");
        dockerConfig.setDockerfileGradlePath("dockerfile-gradle-path");
        when(dockerClient.buildImageCmd()).thenReturn(buildImageCmd);
        when(buildImageCmd.withTags(Set.of(BUILD_IMAGE_NAME))).thenReturn(buildImageCmd);
        when(buildImageCmd.withBaseDirectory(any(File.class))).thenReturn(buildImageCmd);
        when(buildImageCmd.withDockerfile(any(File.class))).thenReturn(buildImageCmd);

        if (!noCache) {
            argsBuilder.cacheFrom(cacheFrom);
        }

        when(buildImageCmd.exec(any(BuildImageResultCallback.class))).thenReturn(buildImageResultCallback);
        when(buildImageResultCallback.awaitImageId()).thenReturn(newImageId);

        final var runChainContext = new RunChainContext(config, argsBuilder.build());
        assertThat(command.execute(runChainContext)).isEqualTo(Command.CONTINUE_PROCESSING);

        if (noCache) {
            verify(buildImageCmd).withNoCache(true);
            verify(removeImageCmd).exec();
        } else {
            verify(buildImageCmd).withCacheFrom(cacheFromCaptor.capture());
            assertThat(cacheFromCaptor.getValue()).containsAll(cacheFrom);
        }
    }

    static Stream<Arguments> shouldHaveBuiltBuildImage() {
        return Stream.of(
                Arguments.of(false, List.of()),
                Arguments.of(true, List.of(randomAlphabetic(16), randomAlphabetic(16)))
        );
    }
}
