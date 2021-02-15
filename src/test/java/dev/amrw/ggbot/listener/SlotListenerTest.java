package dev.amrw.ggbot.listener;

import dev.amrw.ggbot.config.BotConfig;
import dev.amrw.ggbot.dto.Error;
import dev.amrw.ggbot.dto.PlayRequest;
import dev.amrw.ggbot.dto.SlotResult;
import dev.amrw.ggbot.helper.SlotListenerHelper;
import dev.amrw.ggbot.service.SlotService;
import dev.amrw.ggbot.util.DiscordMessageUtil;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageAuthor;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.message.MessageCreateEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SlotListenerTest {

    @Mock
    private BotConfig botConfig;
    @Mock
    private SlotService service;
    @Mock
    private DiscordMessageUtil messageUtil;
    @Mock
    private SlotListenerHelper helper;
    @Spy
    @InjectMocks
    private SlotListener listener;

    private String prefix;
    @Mock
    private MessageCreateEvent event;
    @Mock
    private Message message;
    @Mock
    private TextChannel channel;
    @Mock
    private MessageAuthor messageAuthor;
    @Mock
    private SlotResult slotResult;
    @Mock
    private EmbedBuilder embedBuilder;

    @BeforeEach
    void beforeEach() {
        final var trigger = randomAlphabetic(3);
        prefix = trigger + " " + SlotListener.KEYWORD;
        when(event.getMessage()).thenReturn(message);
        when(botConfig.getTrigger()).thenReturn(trigger);
    }

    @Test
    @DisplayName("Should not have handled a message with wrong prefix")
    void shouldNotHaveHandledMessageWithWrongPrefix() {
        when(message.getContent()).thenReturn(randomAlphanumeric(16));
        listener.onMessageCreate(event);
        verifyNoMoreInteractions(event, message);
        verifyNoInteractions(messageUtil, service, helper);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " help"})
    @DisplayName("Should have sent help message")
    void shouldHaveSentHelpMessage(final String messageContent) {
        when(message.getContent()).thenReturn(prefix + messageContent);
        when(event.getChannel()).thenReturn(channel);
        listener.onMessageCreate(event);
        verify(channel).sendMessage(any(EmbedBuilder.class));
        verifyNoInteractions(messageUtil, service, helper);
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-1", "-123", "abcd", "💯💯💯"})
    @DisplayName("Should have sent an error message in case of an invalid bet")
    void shouldHaveSentErrorMessageOnInvalidBet(final String bet) {
        when(message.getContent()).thenReturn(prefix + " " + bet);
        when(event.getMessageAuthor()).thenReturn(messageAuthor);
        when(messageUtil.buildEmbedError(eq(messageAuthor), anyString())).thenReturn(embedBuilder);
        when(event.getChannel()).thenReturn(channel);

        listener.onMessageCreate(event);

        final var stringCaptor = ArgumentCaptor.forClass(String.class);
        verify(messageUtil).buildEmbedError(eq(messageAuthor), stringCaptor.capture());
        assertThat(stringCaptor.getValue()).containsPattern(
                String.format("(invalid bet|%s)", Error.NEGATIVE_BET.getMessage()));
        verify(channel).sendMessage(embedBuilder);
        verifyNoInteractions(service, helper);
    }

    @ParameterizedTest
    @CsvSource({"INSUFFICIENT_CREDITS", "UNKNOWN_ERROR"})
    @DisplayName("Should have sent an error message when the game has not been played")
    void shouldHaveSentErrorMessageWhenHasNotPlayed(final Error error) {
        when(message.getContent()).thenReturn(prefix + " " + 100);
        when(event.getMessageAuthor()).thenReturn(messageAuthor);
        when(service.play(any(PlayRequest.class))).thenReturn(slotResult);
        when(slotResult.hasPlayed()).thenReturn(false);
        when(slotResult.getError()).thenReturn(error);
        when(event.getChannel()).thenReturn(channel);
        when(messageUtil.buildEmbedError(eq(messageAuthor), anyString())).thenReturn(embedBuilder);

        listener.onMessageCreate(event);

        final var stringCaptor = ArgumentCaptor.forClass(String.class);
        verify(messageUtil).buildEmbedError(eq(messageAuthor), stringCaptor.capture());
        assertThat(stringCaptor.getValue()).isEqualTo(error.getMessage());
        verify(channel).sendMessage(embedBuilder);
        verifyNoMoreInteractions(service);
        verifyNoInteractions(helper);
    }

    @Test
    @DisplayName("Should have played a game of slots and displayed the result")
    void shouldHavePlayedAndDisplayedResult() {
        when(message.getContent()).thenReturn(prefix + " " + 100);
        when(event.getMessageAuthor()).thenReturn(messageAuthor);
        when(service.play(any(PlayRequest.class))).thenReturn(slotResult);
        when(slotResult.hasPlayed()).thenReturn(true);

        listener.onMessageCreate(event);

        verify(helper).displayResultSuspensefully(event, slotResult);
        verifyNoMoreInteractions(service, helper);
    }
}
