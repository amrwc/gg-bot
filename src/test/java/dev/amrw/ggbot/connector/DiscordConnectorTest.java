package dev.amrw.ggbot.connector;

import dev.amrw.ggbot.config.BotConfig;
import dev.amrw.ggbot.config.ConfigReader;
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

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DiscordConnectorTest {

    @Mock
    private ConfigReader configReader;
    @Mock
    private DiscordApiBuilder discordApiBuilder;
    @Mock
    private DiscordApi discordApi;

    @Mock
    private BotConfig botConfig;
    private String authToken;
    @Mock
    private CompletableFuture<DiscordApi> discordApiCompletableFuture;
    @Captor
    private ArgumentCaptor<String> stringCaptor;

    @BeforeEach
    void beforeEach() {
        authToken = randomAlphanumeric(16);
    }

    @Test
    @DisplayName("Should have thrown IllegalStateException when the BotConfig hasn't been present")
    void shouldHaveThrownExceptionWhenBotConfigHasNotBeenPresent() {
        when(configReader.getConfig(BotConfig.PATH, BotConfig.class)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> new DiscordConnector(configReader, discordApiBuilder))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Could not load bot config");
        verifyNoInteractions(discordApiBuilder);
    }

    @Test
    @DisplayName("Should have initialised Discord API")
    void shouldHaveInitialisedDiscordApi() {
        when(configReader.getConfig(BotConfig.PATH, BotConfig.class)).thenReturn(Optional.of(botConfig));
        when(botConfig.getAuthToken()).thenReturn(authToken);
        when(discordApiBuilder.setToken(anyString())).thenReturn(discordApiBuilder);
        when(discordApiBuilder.login()).thenReturn(discordApiCompletableFuture);
        when(discordApiCompletableFuture.join()).thenReturn(discordApi);

        final var connector = new DiscordConnector(configReader, discordApiBuilder);

        assertThat(connector.getApi()).isEqualTo(discordApi);
        verify(discordApiBuilder).setToken(stringCaptor.capture());
        assertThat(stringCaptor.getValue()).isEqualTo(authToken);
        verify(discordApiBuilder).login();
        verify(discordApiCompletableFuture).join();
    }
}
