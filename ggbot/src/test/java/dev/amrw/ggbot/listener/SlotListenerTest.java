package dev.amrw.ggbot.listener;

import dev.amrw.ggbot.dto.Error;
import dev.amrw.ggbot.dto.GameRequest;
import dev.amrw.ggbot.dto.SlotResult;
import dev.amrw.ggbot.helper.SlotListenerHelper;
import dev.amrw.ggbot.service.SlotService;
import dev.amrw.ggbot.util.DiscordMessageUtil;
import org.javacord.api.entity.channel.TextChannel;
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
    private TextChannel channel;
    @Mock
    private SlotResult slotResult;
    @Mock
    private EmbedBuilder embedBuilder;

    private String prefix;

    @BeforeEach
    void beforeEach() {
        final var trigger = randomAlphabetic(3);
        prefix = trigger + " " + SlotListener.KEYWORD;
    }

    @ParameterizedTest
    @ValueSource(strings = {
            // Long overflow
            "" + Long.MAX_VALUE + "123",
            "" + Long.MIN_VALUE + "123",
            // Wrong type
            "123.456",
            "123.456789012345678901234567890",
            // Gibberish
            "abcdef",
    })
    void shouldHaveSentErrorMessageOnInvalidBet(final String bet) {
        testParseCommand(bet);
    }

    @ParameterizedTest
    @ValueSource(strings = {"-1", "0"})
    void shouldHaveSentErrorMessageOnNonPositiveBet(final String bet) {
        testParseCommand(bet);
    }

    @ParameterizedTest
    @CsvSource({"INSUFFICIENT_CREDITS", "UNKNOWN_ERROR"})
    @DisplayName("Should have sent an error message when the game has not been played")
    void shouldHaveSentErrorMessageWhenHasNotPlayed(final Error error) {
        when(event.getMessageContent()).thenReturn(prefix + " " + 100);
        when(service.play(any(GameRequest.class))).thenReturn(slotResult);
        when(slotResult.hasPlayed()).thenReturn(false);
        when(slotResult.getError()).thenReturn(Optional.of(error));
        when(event.getChannel()).thenReturn(channel);
        when(messageUtil.buildError(eq(event), anyString())).thenReturn(embedBuilder);

        listener.process(event);

        final var stringCaptor = ArgumentCaptor.forClass(String.class);
        verify(messageUtil).buildError(eq(event), stringCaptor.capture());
        assertThat(stringCaptor.getValue()).isEqualTo(error.getMessage());
        verify(channel).sendMessage(embedBuilder);
        verifyNoMoreInteractions(service);
        verifyNoInteractions(helper);
    }

    @Test
    @DisplayName("Should have played a game of slots and displayed the result")
    void shouldHavePlayedAndDisplayedResult() {
        when(event.getMessageContent()).thenReturn(prefix + " " + 100);
        when(service.play(any(GameRequest.class))).thenReturn(slotResult);
        when(slotResult.hasPlayed()).thenReturn(true);

        listener.process(event);

        verify(helper).displayResultSuspensefully(event, slotResult);
        verifyNoMoreInteractions(service, helper);
    }

    @ParameterizedTest
    @CsvSource({
            "'', true",
            "' help', true",
            "' 2000 help', true",
            "' 2000', false",
    })
    @DisplayName("Should have determined whether the user needs to see the help message")
    void shouldHaveDeterminedWhetherNeedsHelp(final String content, final boolean expectedResult) {
        when(event.getMessageContent()).thenReturn(prefix + content);
        assertThat(listener.needsHelp(event)).isEqualTo(expectedResult);
    }

    @Test
    void shouldHaveSentHelpMessage() {
        when(messageUtil.buildInfo(event, "Slot Machine")).thenReturn(embedBuilder);
        when(embedBuilder.addField(eq("Rules"), anyString())).thenReturn(embedBuilder);
        doReturn(prefix).when(listener).getPrefix();
        when(embedBuilder.addField(eq("Usage"), anyString())).thenReturn(embedBuilder);
        when(event.getChannel()).thenReturn(channel);

        listener.showHelp(event);

        verify(channel).sendMessage(embedBuilder);
        verifyNoMoreInteractions(messageUtil, channel);
        verifyNoInteractions(service, helper);
    }

    private void testParseCommand(final String bet) {
        when(event.getMessageContent()).thenReturn(String.format("%s %s", prefix, bet));
        Mockito.lenient().when(messageUtil.buildInvalidCommandError(event, prefix)).thenReturn(embedBuilder);
        Mockito.lenient().when(messageUtil.buildError(event, Error.NEGATIVE_BET)).thenReturn(embedBuilder);
        when(event.getChannel()).thenReturn(channel);
        Mockito.lenient().doReturn(prefix).when(listener).getPrefix();

        listener.process(event);

        verify(channel).sendMessage(embedBuilder);
    }
}
