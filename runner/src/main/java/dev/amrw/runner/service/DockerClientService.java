package dev.amrw.runner.service;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;

public class DockerClientService {

    public DockerClient buildDefaultDockerClient() {
        final var dockerClientConfig = DefaultDockerClientConfig
                .createDefaultConfigBuilder()
                .build();
        final var httpClient = new ApacheDockerHttpClient.Builder()
                .dockerHost(dockerClientConfig.getDockerHost())
                .sslConfig(dockerClientConfig.getSSLConfig())
                .build();
        return DockerClientImpl.getInstance(dockerClientConfig, httpClient);
    }
}
