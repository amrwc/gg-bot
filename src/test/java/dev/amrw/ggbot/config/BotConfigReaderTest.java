package dev.amrw.ggbot.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BotConfigReaderTest {

    @Mock
    private ObjectMapper objectMapper;
    @Spy
    @InjectMocks
    private BotConfigReader reader;

    @Mock
    private InputStream botConfigStream;
    @Mock
    private BotConfig botConfig;

    @Test
    @DisplayName("Should have handled an IOException when reading the bot config resource")
    void shouldHaveHandledIOException() throws IOException {
        when(reader.readBotConfig()).thenReturn(botConfigStream);
        when(objectMapper.readValue(botConfigStream, BotConfig.class)).thenThrow(new IOException());
        assertThat(reader.getBotConfig()).isEqualTo(Optional.empty());
    }

    @Test
    @DisplayName("Should have gotten the bot config instance")
    void shouldHaveGottenBotConfig() throws IOException {
        when(reader.readBotConfig()).thenReturn(botConfigStream);
        when(objectMapper.readValue(botConfigStream, BotConfig.class)).thenReturn(botConfig);
        assertThat(reader.getBotConfig()).isEqualTo(Optional.of(botConfig));
    }
}
