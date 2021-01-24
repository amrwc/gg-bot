package me.rename.renameme;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Tag(TestType.IntegrationTest)
class ApplicationIntegrationTest {

    @Test
    @DisplayName("Should have loaded application context")
    void shouldHaveLoadedApplicationContext() {
    }
}
