package dev.amrw.ggbot.listener;

import dev.amrw.ggbot.dto.Error;
import dev.amrw.ggbot.dto.GameRequest;
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
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SlotListenerTest {

    @Mock
    private SlotService service;
    @Mock
    private DiscordMessageUtil messageUtil;
    @Mock
    private SlotListenerHelper helper;
    @Spy
    @InjectMocks
    private SlotListener listener;

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

    private String prefix;

    @BeforeEach
    void beforeEach() {
        final var trigger = randomAlphabetic(3);
        prefix = trigger + " " + SlotListener.KEYWORD;
        when(event.getMessage()).thenReturn(message);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " help"})
    @DisplayName("Should have sent help message")
    void shouldHaveSentHelpMessage(final String messageContent) {
        when(message.getContent()).thenReturn(prefix + messageContent);
        when(messageUtil.buildEmbedInfo(event, "Slot Machine")).thenReturn(embedBuilder);
        when(embedBuilder.addField(eq("Rules"), anyString())).thenReturn(embedBuilder);
        doReturn(prefix).when(listener).getPrefix();
        when(embedBuilder.addField(eq("Usage"), anyString())).thenReturn(embedBuilder);
        when(event.getChannel()).thenReturn(channel);

        listener.process(event);

        verify(channel).sendMessage(embedBuilder);
        verifyNoMoreInteractions(messageUtil, channel);
        verifyNoInteractions(service, helper);
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-1", "-123", "abcd", "💯💯💯"})
    @DisplayName("Should have sent an error message in case of an invalid bet")
    void shouldHaveSentErrorMessageOnInvalidBet(final String bet) {
        when(message.getContent()).thenReturn(prefix + " " + bet);
        when(event.getMessageAuthor()).thenReturn(messageAuthor);
        Mockito.lenient().doReturn(prefix).when(listener).getPrefix();
        when(messageUtil.buildEmbedError(eq(event), anyString())).thenReturn(embedBuilder);
        when(event.getChannel()).thenReturn(channel);

        listener.process(event);

        final var stringCaptor = ArgumentCaptor.forClass(String.class);
        verify(messageUtil).buildEmbedError(eq(event), stringCaptor.capture());
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
        when(service.play(any(GameRequest.class))).thenReturn(slotResult);
        when(slotResult.hasPlayed()).thenReturn(false);
        when(slotResult.getError()).thenReturn(Optional.of(error));
        when(event.getChannel()).thenReturn(channel);
        when(messageUtil.buildEmbedError(eq(event), anyString())).thenReturn(embedBuilder);

        listener.process(event);

        final var stringCaptor = ArgumentCaptor.forClass(String.class);
        verify(messageUtil).buildEmbedError(eq(event), stringCaptor.capture());
        assertThat(stringCaptor.getValue()).isEqualTo(error.getMessage());
        verify(channel).sendMessage(embedBuilder);
        verifyNoMoreInteractions(service);
        verifyNoInteractions(helper);
    }

    @Test
    @DisplayName("Should have played a game of slots and displayed the result")
    void shouldHavePlayedAndDisplayedResult() {
        when(message.getContent()).thenReturn(prefix + " " + 100);
        when(service.play(any(GameRequest.class))).thenReturn(slotResult);
        when(slotResult.hasPlayed()).thenReturn(true);

        listener.process(event);

        verify(helper).displayResultSuspensefully(event, slotResult);
        verifyNoMoreInteractions(service, helper);
    }
}
