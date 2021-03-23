package dev.amrw.bin.chain.context;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import lombok.Getter;
import org.apache.commons.chain.impl.ContextBase;

/**
 * Context of the Run Chain.
 */
@Getter
public class RunChainContext extends ContextBase {

    private final DockerClient dockerClient;

    public RunChainContext() {
        super();
        final var config = DefaultDockerClientConfig
                .createDefaultConfigBuilder()
                .build();
        final var httpClient = new ApacheDockerHttpClient.Builder()
                .dockerHost(config.getDockerHost())
                .sslConfig(config.getSSLConfig())
                .build();
        dockerClient = DockerClientImpl.getInstance(config, httpClient);
    }
}
