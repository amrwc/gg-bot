package dev.amrw.bin.command;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;

public class PrepareDockerEnvironment implements Command {

    @Override
    public boolean execute(final Context context) {
        DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder().build();
        DockerHttpClient httpClient = new ApacheDockerHttpClient.Builder()
                .dockerHost(config.getDockerHost())
                .sslConfig(config.getSSLConfig())
                .build();
        DockerClient dockerClient = DockerClientImpl.getInstance(config, httpClient);
        final var containers = dockerClient.listContainersCmd().exec();
        System.out.println(containers);
        return Command.PROCESSING_COMPLETE;
    }
}
