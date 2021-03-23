package dev.amrw.bin;

import dev.amrw.bin.subcommand.Run;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.util.concurrent.Callable;

import static picocli.CommandLine.*;

/**
 * The bin commands', and the whole application's entry point.
 */
@Command(
        name = "bin",
        version = "0.0.1",
        mixinStandardHelpOptions = true,
        description = "Parent runner of commands.",
        subcommands = {
                Run.class,
        }
)
public class Application implements Callable<Integer> {

    public static void main(final String[] argv) {
        final var exitCode = new CommandLine(new Application()).execute(argv);
        System.exit(exitCode);
    }

    @Override
    public Integer call() {
        return ExitCode.OK;
    }
}
