package dev.amrw.bin.chain.command;

import com.github.dockerjava.api.DockerClient;
import dev.amrw.bin.chain.context.RunChainContext;
import dev.amrw.bin.dto.RunArgs;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;

/**
 * Base command of the Run chain.
 */
abstract class RunChainCommand implements Command {

    RunChainContext runChainContext;
    RunArgs args;
    DockerClient dockerClient;

    void prepareContext(final Context context) {
        this.runChainContext = (RunChainContext) context;
        this.args = this.runChainContext.getArgs();
        this.dockerClient = this.runChainContext.getDockerClient();
    }
}
