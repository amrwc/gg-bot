package dev.amrw.runner.dto;

import dev.amrw.runner.subcommand.Run;
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

    private RunArgs(final Builder builder) {
        this.applyMigrations = builder.applyMigrations;
        this.cacheFrom = builder.cacheFrom;
        this.debug = builder.debug;
        this.detach = builder.detach;
        this.noCache = builder.noCache;
        this.rebuild = builder.rebuild;
        this.startDb = builder.startDb;
        this.suspend = builder.suspend;
    }

    public static class Builder {

        private boolean applyMigrations;
        private List<String> cacheFrom;
        private boolean debug;
        private boolean detach;
        private boolean noCache;
        private boolean rebuild;
        private boolean startDb;
        private boolean suspend;

        public Builder withApplyMigrations(final boolean applyMigrations) {
            this.applyMigrations = applyMigrations;
            return this;
        }

        public Builder withCacheFrom(final List<String> cacheFrom) {
            this.cacheFrom = cacheFrom;
            return this;
        }

        public Builder withDebug(final boolean debug) {
            this.debug = debug;
            return this;
        }

        public Builder withDetach(final boolean detach) {
            this.detach = detach;
            return this;
        }

        public Builder withNoCache(final boolean noCache) {
            this.noCache = noCache;
            return this;
        }

        public Builder withRebuild(final boolean rebuild) {
            this.rebuild = rebuild;
            return this;
        }

        public Builder withStartDb(final boolean startDb) {
            this.startDb = startDb;
            return this;
        }

        public Builder withSuspend(final boolean suspend) {
            this.suspend = suspend;
            return this;
        }

        public RunArgs build() {
            return new RunArgs(this);
        }
    }
}
