package dev.amrw.bin.subcommand;

import dev.amrw.bin.chain.RunChain;
import dev.amrw.bin.chain.context.RunChainContext;
import dev.amrw.bin.dto.RunArgs;
import dev.amrw.bin.util.ConfigReader;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.log4j.Log4j2;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.List;
import java.util.concurrent.Callable;

import static picocli.CommandLine.ExitCode;

/**
 * Build and run the application.
 */
@Log4j2
@Getter
@Accessors(fluent = true)
@Command(
        name = "run",
        mixinStandardHelpOptions = true,
        description = "Build and run GG Bot Docker container."
)
public class Run implements Callable<Integer> {

    @Option(names = {"--apply-migrations"}, description = "Apply database migrations. Includes `--start-db`.")
    private boolean applyMigrations = false;
    @Option(names = {"--cache-from"}, description = "Docker image(s) to reuse the layers of.")
    private List<String> cacheFrom = List.of();
    @Option(names = {"--debug"}, description = "Enable Tomcat debug port.")
    private boolean debug = false;
    @Option(names = {"--detach"}, description = "Detach the Docker container.")
    private boolean detach = false;
    @Option(names = {"--no-cache"}, description = "Don't use cache for the build.")
    private boolean noCache = false;
    @Option(names = {"--rebuild"}, description = "Recompile the application, and rebuild the main container.")
    private boolean rebuild = false;
    @Option(names = {"--start-db"}, description = "Start the database container.")
    private boolean startDb = false;
    @Option(names = {"--suspend"}, description =
            "Suspend the web server until the remote debugger has connected. Includes `--debug`.")
    private boolean suspend = false;

    @Override
    public Integer call() {
        final var configReader = new ConfigReader();
        final var config = configReader.getDefaultConfig()
                .orElseThrow(() -> new IllegalArgumentException("Error reading default config"));
        final var args = new RunArgs(this);
        final var context = new RunChainContext(config, args);
        final var chain = new RunChain();

        boolean result;
        try {
            result = chain.execute(context);
        } catch (final Exception exception) {
            log.debug("Error executing chain {}", chain.getClass(), exception);
            result = false;
        }

        return result ? ExitCode.OK : ExitCode.SOFTWARE;
    }
}
