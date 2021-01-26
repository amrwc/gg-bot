package dev.amrw.ggbot.connector;

import dev.amrw.ggbot.config.BotConfigReader;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.CompletableFuture;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DiscordConnectorTest {

    @Mock
    private DiscordApiBuilder discordApiBuilder;
    @Mock
    private BotConfigReader botConfigReader;
    @Mock
    private DiscordApi discordApi;

    String authToken;
    @Mock
    private CompletableFuture<DiscordApi> discordApiCompletableFuture;
    @Captor
    private ArgumentCaptor<String> stringCaptor;

    @BeforeEach
    void beforeEach() {
        authToken = randomAlphanumeric(16);
    }

    @Test
    @DisplayName("Should have initialised Discord API")
    void shouldHaveInitialisedDiscordApi() {
        when(botConfigReader.getAuthToken()).thenReturn(authToken);
        when(discordApiBuilder.setToken(anyString())).thenReturn(discordApiBuilder);
        when(discordApiBuilder.login()).thenReturn(discordApiCompletableFuture);
        when(discordApiCompletableFuture.join()).thenReturn(discordApi);

        final var connector = new DiscordConnector(discordApiBuilder, botConfigReader);

        assertThat(connector.getApi()).isEqualTo(discordApi);
        verify(discordApiBuilder).setToken(stringCaptor.capture());
        assertThat(stringCaptor.getValue()).isEqualTo(authToken);
    }
}
