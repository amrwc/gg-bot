package dev.amrw.bin.chain;

import dev.amrw.bin.chain.command.BuildBuildImage;
import dev.amrw.bin.chain.command.PrepareDockerEnvironment;
import dev.amrw.bin.chain.command.RunBuildImage;
import org.apache.commons.chain.impl.ChainBase;

public class RunChain extends ChainBase {

    public RunChain() {
        super();
        addCommand(new PrepareDockerEnvironment());
        addCommand(new BuildBuildImage());
        addCommand(new RunBuildImage());
    }
}
