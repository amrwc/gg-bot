package dev.amrw.runner.chain.run.command;

import com.github.dockerjava.api.DockerClient;
import dev.amrw.runner.chain.run.RunChainContext;
import dev.amrw.runner.chain.run.helper.RunChainHelper;
import dev.amrw.runner.config.Config;
import dev.amrw.runner.config.DockerConfig;
import dev.amrw.runner.dto.RunArgs;
import dev.amrw.runner.helper.DockerClientHelper;
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
    Config config;
    DockerConfig dockerConfig;
    RunChainHelper runChainHelper;

    void prepareContext(final Context context) {
        this.runChainContext = (RunChainContext) context;
        this.args = this.runChainContext.getArgs();
        this.dockerClient = this.runChainContext.getDockerClient();
        this.dockerClientHelper = this.runChainContext.getDockerClientHelper();
        this.config = this.runChainContext.getConfig();
        this.dockerConfig = config.getDockerConfig();
        this.runChainHelper = getRunChainHelper();
    }

    RunChainHelper getRunChainHelper() {
        return new RunChainHelper(dockerConfig, dockerClientHelper);
    }
}
