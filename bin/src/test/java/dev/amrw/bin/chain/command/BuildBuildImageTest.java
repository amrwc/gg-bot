package dev.amrw.bin.chain.command;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.BuildImageCmd;
import com.github.dockerjava.api.command.BuildImageResultCallback;
import com.github.dockerjava.api.command.ListImagesCmd;
import com.github.dockerjava.api.command.RemoveImageCmd;
import com.github.dockerjava.api.model.Image;
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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BuildBuildImageTest {

    @InjectMocks
    private BuildBuildImage command;

    @Mock
    private RunChainContext context;
    @Mock
    private Config config;
    @Mock
    private DockerConfig dockerConfig;
    @Mock
    private BuildImageConfig buildImageConfig;
    @Mock
    private RunArgs args;
    @Mock
    private DockerClient dockerClient;

    @Mock
    private ListImagesCmd listImagesCmd;
    @Mock
    private Image image;

    @Mock
    private BuildImageCmd buildImageCmd;
    @Captor
    private ArgumentCaptor<Set<String>> cacheFromCaptor;

    private String buildImageName;

    @BeforeEach
    void beforeEach() {
        buildImageName = randomAlphabetic(16);

        when(context.getArgs()).thenReturn(args);
        when(context.getConfig()).thenReturn(config);
        when(context.getDockerClient()).thenReturn(dockerClient);

        when(config.getDockerConfig()).thenReturn(dockerConfig);

        when(dockerConfig.getBuildImageConfig()).thenReturn(buildImageConfig);
        when(buildImageConfig.getName()).thenReturn(buildImageName);

        when(dockerClient.listImagesCmd()).thenReturn(listImagesCmd);
        when(listImagesCmd.exec()).thenReturn(List.of(image));
        when(image.getRepoTags()).thenReturn(new String[] {buildImageName + ":latest"});
    }

    @Test
    @DisplayName("Should have skipped building the build image if it already exists")
    void shouldHaveSkippedWhenImageExists() {
        when(args.rebuild()).thenReturn(false);
        assertThat(command.execute(context)).isEqualTo(Command.CONTINUE_PROCESSING);
    }

    @ParameterizedTest
    @MethodSource
    @DisplayName("Should have built the build image")
    void shouldHaveBuiltBuildImage(final boolean noCache, final List<String> cacheFrom) {
        final var removeImageCmd = mock(RemoveImageCmd.class);
        final var callback = mock(BuildImageResultCallback.class);
        final var existingImageId = randomAlphanumeric(32);
        final var newImageId = "sha256:" + randomAlphanumeric(32);

        when(args.rebuild()).thenReturn(true);

        when(args.noCache()).thenReturn(noCache);
        if (noCache) {
            when(image.getId()).thenReturn(existingImageId);
            when(dockerClient.removeImageCmd(existingImageId)).thenReturn(removeImageCmd);
        }

        when(dockerConfig.getBaseDirPath()).thenReturn(randomAlphabetic(16));
        when(dockerConfig.getDockerfileGradlePath()).thenReturn(randomAlphabetic(16));
        when(dockerClient.buildImageCmd()).thenReturn(buildImageCmd);
        when(buildImageCmd.withTags(Set.of(buildImageName))).thenReturn(buildImageCmd);
        when(buildImageCmd.withBaseDirectory(any(File.class))).thenReturn(buildImageCmd);
        when(buildImageCmd.withDockerfile(any(File.class))).thenReturn(buildImageCmd);

        if (!noCache) {
            when(args.cacheFrom()).thenReturn(cacheFrom);
        }

        when(buildImageCmd.exec(any(BuildImageResultCallback.class))).thenReturn(callback);
        when(callback.awaitImageId()).thenReturn(newImageId);

        assertThat(command.execute(context)).isEqualTo(Command.CONTINUE_PROCESSING);

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
