package dev.amrw.runner.chain.run.command;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

// TODO:
@ExtendWith(MockitoExtension.class)
class StartMainContainerTest extends RunChainCommandTestBase {

    private StartMainContainer command;

    @BeforeEach
    void beforeEach() {
        super.beforeEach();

        command = new StartMainContainer();
    }
}
