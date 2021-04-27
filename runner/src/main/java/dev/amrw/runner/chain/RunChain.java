package dev.amrw.runner.chain;

import dev.amrw.runner.chain.command.*;
import org.apache.commons.chain.impl.ChainBase;

public class RunChain extends ChainBase {

    public RunChain() {
        super();
        addCommand(new CheckDockerComponents());
        addCommand(new PrepareDockerEnvironment());
        addCommand(new BuildBuildImage());
        addCommand(new CreateBuildContainer());
        addCommand(new StartBuildContainer());
        addCommand(new CopyJarFromBuildContainer());
    }
}