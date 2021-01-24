package me.rename.renameme;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ApplicationTest {

    @Test
    @DisplayName("Should have returned the welcome message")
    void shouldHaveWelcomed() {
        assertThat(Application.getWelcomeMessage()).isEqualTo(Application.WELCOME_MESSAGE);
    }
}
