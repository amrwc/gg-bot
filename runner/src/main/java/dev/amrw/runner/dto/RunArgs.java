package dev.amrw.runner.dto;

import dev.amrw.runner.subcommand.Run;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * CLI arguments for {@link Run} subcommand.
 */
@Getter
@Builder
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
}
