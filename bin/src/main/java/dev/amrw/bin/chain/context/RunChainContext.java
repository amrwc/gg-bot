package dev.amrw.bin.chain.context;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import dev.amrw.bin.DockerClientHelper;
import dev.amrw.bin.config.Config;
import dev.amrw.bin.dto.RunArgs;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.chain.impl.ContextBase;

/**
 * Context of the Run Chain.
 */
@Getter
public class RunChainContext extends ContextBase {

    private final Config config;
    private final RunArgs args;
    private final DockerClient dockerClient;
    private final DockerClientHelper dockerClientHelper;

    @Setter
    @Accessors(fluent = true)
    private boolean networkExists;
    @Setter
    @Accessors(fluent = true)
    private boolean buildCacheVolumeExists;
    @Setter
    @Accessors(fluent = true)
    private boolean buildImageExists;
    @Setter
    @Accessors(fluent = true)
    private boolean buildContainerExists;

    public RunChainContext(final Config config, final RunArgs args) {
        super();
        this.args = args;
        this.config = config;
        final var dockerClientConfig = DefaultDockerClientConfig
                .createDefaultConfigBuilder()
                .build();
        final var httpClient = new ApacheDockerHttpClient.Builder()
                .dockerHost(dockerClientConfig.getDockerHost())
                .sslConfig(dockerClientConfig.getSSLConfig())
                .build();
        this.dockerClient = DockerClientImpl.getInstance(dockerClientConfig, httpClient);
        this.dockerClientHelper = new DockerClientHelper(this.dockerClient);
    }
}
