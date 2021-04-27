package dev.amrw.runner.chain.command;

import com.github.dockerjava.api.DockerClient;
import dev.amrw.runner.helper.DockerClientHelper;
import dev.amrw.runner.chain.context.RunChainContext;
import dev.amrw.runner.config.DockerConfig;
import dev.amrw.runner.dto.RunArgs;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;

/**
 * Base command of the Run chain.
 */
abstract class RunChainCommand implements Command {

    RunChainContext runChainContext;
    RunArgs args;
    DockerClient dockerClient;
    DockerClientHelper dockerClientHelper;
    DockerConfig dockerConfig;

    void prepareContext(final Context context) {
        this.runChainContext = (RunChainContext) context;
        this.args = this.runChainContext.getArgs();
        this.dockerClient = this.runChainContext.getDockerClient();
        this.dockerClientHelper = this.runChainContext.getDockerClientHelper();
        final var config = this.runChainContext.getConfig();
        this.dockerConfig = config.getDockerConfig();
    }
}
