package dev.amrw.ggbot.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.io.InputStream;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BotConfigReaderTest {

    @Mock
    private ObjectMapper objectMapper;
    @InjectMocks
    private BotConfigReader reader;

    @Mock
    private BotConfig botConfig;
    private String authToken;

    @BeforeEach
    void beforeEach() {
        authToken = randomAlphanumeric(16);
    }

    @Test
    @Disabled("Since the bot-config.yml is Git-ignored, this test will fail in the CICD pipeline.")
    @DisplayName("Should have gotten the authentication token")
    void shouldHaveGottenAuthToken() throws IOException {
        when(objectMapper.readValue(any(InputStream.class), eq(BotConfig.class))).thenReturn(botConfig);
        when(botConfig.getAuthToken()).thenReturn(authToken);

        final var result = reader.getAuthToken();

        assertThat(result).isEqualTo(authToken);
    }
}
