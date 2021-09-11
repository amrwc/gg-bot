package dev.amrw.runner.chain.run;

import com.github.dockerjava.api.DockerClient;
import dev.amrw.runner.config.Config;
import dev.amrw.runner.config.DockerConfig;
import dev.amrw.runner.dto.RunArgs;
import dev.amrw.runner.service.DockerClientService;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;

/**
 * Base command of the Run chain.
 */
public class RunChainCommandBase implements Command {

    protected final DockerClientService dockerClientService;

    protected RunChainContext runChainContext;
    protected RunArgs args;
    protected Config config;
    protected DockerConfig dockerConfig;

    protected RunChainCommandBase() {
        dockerClientService = DockerClientService.getInstance();
    }

    protected RunChainCommandBase(final DockerClientService dockerClientService) {
        this.dockerClientService = dockerClientService;
    }

    @Override
    public boolean execute(final Context context) throws Exception {
        return Command.PROCESSING_COMPLETE;
    }

    public void prepareContext(final Context context) {
        runChainContext = (RunChainContext) context;
        args = runChainContext.getArgs();
        config = runChainContext.getConfig();
        dockerConfig = config.getDockerConfig();
    }

    protected DockerClient getDockerClient() {
        return dockerClientService.getDockerClient();
    }
}
