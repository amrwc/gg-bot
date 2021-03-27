package dev.amrw.bin.chain;

import dev.amrw.bin.chain.command.*;
import org.apache.commons.chain.impl.ChainBase;

public class RunChain extends ChainBase {

    public RunChain() {
        super();
        addCommand(new CheckDockerComponents());
        addCommand(new PrepareDockerEnvironment());
        addCommand(new BuildBuildImage());
        addCommand(new CreateBuildContainer());
        addCommand(new StartBuildContainer());
    }
}
