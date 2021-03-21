package dev.amrw.bin;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.util.concurrent.Callable;

/**
 * The bin scripts' entry point.
 */
@Command(
        name = "bin",
        version = "0.0.1",
        mixinStandardHelpOptions = true,
        description = "Parent runner of bin scripts."
)
public class Application implements Callable<Integer> {

    private static String[] argv;
    @Parameters(index = "0", description = "Bin script to launch.")
    private String scriptName;

    public static void main(final String[] argv) {
        Application.argv = argv;
        final int exitCode = new CommandLine(new Application()).execute(argv);
        System.exit(exitCode);
    }

    @Override
    public Integer call() throws Exception {
        switch (scriptName) {
            case "run":
                return new CommandLine(new Run()).execute("run", "arg1", "arg2");
            default:
                return 0;
        }
    }
}
