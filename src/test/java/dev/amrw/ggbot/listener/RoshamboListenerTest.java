package dev.amrw.ggbot.listener;

import dev.amrw.ggbot.dto.Error;
import dev.amrw.ggbot.dto.RoshamboRequest;
import dev.amrw.ggbot.dto.RoshamboResult;
import dev.amrw.ggbot.dto.RoshamboShape;
import dev.amrw.ggbot.helper.RoshamboListenerHelper;
import dev.amrw.ggbot.service.RoshamboService;
import dev.amrw.ggbot.util.DiscordMessageUtil;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
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

import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.apache.commons.lang3.RandomUtils.nextLong;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoshamboListenerTest {

    @Mock
    private RoshamboService service;
    @Mock
    private DiscordMessageUtil messageUtil;
    @Mock
    private RoshamboListenerHelper helper;
    @Spy
    @InjectMocks
    private RoshamboListener listener;

    @Mock
    private MessageCreateEvent event;
    @Mock
    private Message message;
    @Mock
    private TextChannel channel;
    @Mock
    private EmbedBuilder embedBuilder;
    @Mock
    private RoshamboResult roshamboResult;

    private String prefix;

    @BeforeEach
    void beforeEach() {
        final var trigger = randomAlphabetic(3);
        prefix = trigger + " " + RoshamboListener.KEYWORD;
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
        testParseCommand(bet, "rock");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "abcd", "notrock"})
    void shouldHaveSentErrorOnInvalidShape(final String shape) {
        testParseCommand("123", shape);
    }

    @ParameterizedTest
    @ValueSource(strings = {"-1", "0"})
    void shouldHaveSentErrorMessageOnNonPositiveBet(final String bet) {
        testParseCommand(bet, "rock");
    }

    @Test
    @DisplayName("Should have played a game of Roshambo and displayed the result")
    void shouldHavePlayedAndDisplayedResult() {
        final long bet = nextLong();
        when(event.getMessageContent()).thenReturn(String.format("%s %s scissors", prefix, bet));
        when(event.getMessage()).thenReturn(message);
        when(service.play(any(RoshamboRequest.class))).thenReturn(roshamboResult);

        listener.process(event);

        verify(message).addReaction(RoshamboShape.SCISSORS.getEmoji());
        final var roshamboRequestCaptor = ArgumentCaptor.forClass(RoshamboRequest.class);
        verify(helper).displayResultSuspensefully(eq(event), roshamboRequestCaptor.capture(), eq(roshamboResult));

        final var expectedRequest = new RoshamboRequest();
        expectedRequest.setEvent(event);
        expectedRequest.setBet(bet);
        expectedRequest.setShape(RoshamboShape.SCISSORS);
        assertThat(roshamboRequestCaptor.getValue()).usingRecursiveComparison().isEqualTo(expectedRequest);
    }

    @ParameterizedTest
    @CsvSource({
            "'', true",
            "'!gg', true",
            "'!gg roshambo', true",
            "'!gg roshambo help', true",
            "'!gg roshambo 100', true",
            "'!gg roshambo 100 help', true",
            "'!gg roshambo 100 rock help', true",

            "'!gg roshambo 100 abcd', false",
            "'!gg roshambo 100 rock', false",
    })
    @DisplayName("Should have determined whether the user needs to see the help message")
    void shouldHaveDeterminedWhetherNeedsHelp(final String content, final boolean expectedResult) {
        when(event.getMessageContent()).thenReturn(content);
        assertThat(listener.needsHelp(event)).isEqualTo(expectedResult);
        verifyNoMoreInteractions(event);
    }

    @Test
    @DisplayName("Should have shown help to the user")
    void shouldHaveShownHelp() {
        when(messageUtil.buildInfo(event, "Roshambo")).thenReturn(embedBuilder);
        when(embedBuilder.addField(eq("Rules"), anyString())).thenReturn(embedBuilder);
        doReturn(prefix).when(listener).getPrefix();
        when(embedBuilder.addField(eq("Usage"), anyString())).thenReturn(embedBuilder);
        when(event.getChannel()).thenReturn(channel);

        listener.showHelp(event);

        verify(channel).sendMessage(embedBuilder);
    }

    private void testParseCommand(final String bet, final String shape) {
        when(event.getMessageContent()).thenReturn(String.format("%s %s %s", prefix, bet, shape));
        Mockito.lenient().when(messageUtil.buildInvalidCommandError(event, prefix)).thenReturn(embedBuilder);
        Mockito.lenient().when(messageUtil.buildError(event, Error.NEGATIVE_BET)).thenReturn(embedBuilder);
        when(event.getChannel()).thenReturn(channel);
        Mockito.lenient().doReturn(prefix).when(listener).getPrefix();

        listener.process(event);

        verify(channel).sendMessage(embedBuilder);
    }
}
