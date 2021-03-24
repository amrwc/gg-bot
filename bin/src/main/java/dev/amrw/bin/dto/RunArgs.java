package dev.amrw.bin.dto;

import dev.amrw.bin.subcommand.Run;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * CLI arguments for {@link Run} subcommand.
 */
@Getter
@Accessors(fluent = true)
public class RunArgs {

    private final boolean applyMigrations;
    private final List<String> cacheFrom;
    private final boolean debug;
    private final boolean detach;
    private final boolean noCache;
    private final boolean rebuild;
    private final boolean startDb;
    private final boolean suspend;

    public RunArgs(final Run command) {
        applyMigrations = command.applyMigrations();
        cacheFrom = command.cacheFrom();
        debug = command.debug();
        detach = command.detach();
        noCache = command.noCache();
        rebuild = command.rebuild();
        startDb = command.startDb();
        suspend = command.suspend();
    }
}
