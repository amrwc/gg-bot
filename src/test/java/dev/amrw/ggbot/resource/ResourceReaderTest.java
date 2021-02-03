package dev.amrw.ggbot.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
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

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResourceReaderTest {

    @Mock
    private ObjectMapper objectMapper;
    @Spy
    @InjectMocks
    private ResourceReader reader;

    private String configPath;
    @Mock
    private InputStream botConfigStream;
    @Mock
    private BotConfig botConfig;

    @BeforeEach
    void setUp() {
        configPath = randomAlphanumeric(16);
    }

    @Test
    @DisplayName("Should have handled an IOException when reading the bot config resource")
    void shouldHaveHandledIOException() throws IOException {
        when(reader.getResourceAsStream(configPath)).thenReturn(botConfigStream);
        when(objectMapper.readValue(botConfigStream, BotConfig.class)).thenThrow(new IOException());
        assertThat(reader.readResource(configPath, BotConfig.class)).isEqualTo(Optional.empty());
    }

    @Test
    @DisplayName("Should have got the bot config instance")
    void shouldHaveGotBotConfig() throws IOException {
        when(reader.getResourceAsStream(configPath)).thenReturn(botConfigStream);
        when(objectMapper.readValue(botConfigStream, BotConfig.class)).thenReturn(botConfig);
        assertThat(reader.readResource(configPath, BotConfig.class)).isEqualTo(Optional.of(botConfig));
    }
}
