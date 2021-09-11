package dev.amrw.runner.chain.run.command;

import org.junit.jupiter.api.BeforeEach;

// TODO:
class ExtractJarFromArchiveTest extends RunChainCommandTestBase {

    private ExtractJarFromArchive command;

    @BeforeEach
    void beforeEach() {
        super.beforeEach();

        command = new ExtractJarFromArchive();
    }
}
