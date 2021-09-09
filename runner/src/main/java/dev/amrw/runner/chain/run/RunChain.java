package dev.amrw.runner.chain.run;

import dev.amrw.runner.chain.run.command.*;
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
        addCommand(new BuildMainImage());
        addCommand(new CreateMainContainer());
        addCommand(new ExtractJarFromArchive());
        addCommand(new CopyJarIntoMainContainer());
        addCommand(new StartMainContainer());
    }
}
