package dev.amrw.bin.chain;

import dev.amrw.bin.command.PrepareDockerEnvironment;
import org.apache.commons.chain.impl.ChainBase;

public class RunChain extends ChainBase {

    public RunChain() {
        super();
        addCommand(new PrepareDockerEnvironment());
    }
}
