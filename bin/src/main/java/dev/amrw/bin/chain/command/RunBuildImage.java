package dev.amrw.bin.chain.command;

import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;

/**
 * Runs the build image.
 */
public class RunBuildImage extends RunChainCommand {

    @Override
    public boolean execute(final Context context) {
        super.prepareContext(context);

        // return Command.CONTINUE_PROCESSING;
        return Command.PROCESSING_COMPLETE; // TEMP: Remove when the next command has been built
    }
}
