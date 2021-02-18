package dev.amrw.ggbot.listener;

import org.javacord.api.entity.message.Message;
import org.javacord.api.event.message.MessageCreateEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MessageListenerTest {

    @Spy
    @InjectMocks
    private Listener listener;

    @Mock
    private MessageCreateEvent event;
    @Mock
    private Message message;

    @Test
    @DisplayName("Should not have processed the given event if the message content has no matching prefix")
    void shouldNotHaveProcessedEventWithoutMatchingPrefix() {
        doReturn(false).when(listener).hasMatchingPrefix(event);
        listener.onMessageCreate(event);
        verifyNoInteractions(event);
    }

    @Test
    @DisplayName("Should not have processed the given event and shown help")
    void shouldNotHaveProcessedEventAndShownMessage() {
        doReturn(true).when(listener).hasMatchingPrefix(event);
        doReturn(true).when(listener).needsHelp(event);
        doNothing().when(listener).showHelp(event);
        listener.onMessageCreate(event);
        verify(listener).showHelp(event);
        verifyNoInteractions(event);
    }

    @Test
    @DisplayName("Should have processed the given event")
    void shouldHaveProcessedEvent() {
        doReturn(true).when(listener).hasMatchingPrefix(event);
        doReturn(false).when(listener).needsHelp(event);
        doNothing().when(listener).process(event);
        listener.onMessageCreate(event);
        verify(listener).process(event);
        verifyNoInteractions(event);
    }

    @ParameterizedTest
    @CsvSource({
            "'abcd', false",
            "'abcd defg', false",
            "'!abcd', false",
            "'!gg abcd', false",
            "'!gg listener', true",
            "'!gg listener abcd', true",
    })
    @DisplayName("Should have determined whether the message content has a matching prefix")
    void shouldHaveVerifiedPrefix(final String content, final boolean expectedResult) {
        when(event.getMessage()).thenReturn(message);
        when(message.getContent()).thenReturn(content);
        assertThat(listener.hasMatchingPrefix(event)).isEqualTo(expectedResult);
    }

    @ParameterizedTest
    @CsvSource({
            "'!gg listener', false",
            "'!gg listener abcd', false",
            "'!gg listener abcd defg', false",
            "'', true",
            "'!gg', true",
            "'!gg listener help', true",
            "'!gg listener help abcd', true",
            // Special condition -- `HelpListener` should take over
            "'!gg help', false",
            "'!gg help abcd', false",
    })
    @DisplayName("Should have determined whether the user needs to see the help message")
    void shouldHaveDeterminedWhetherNeedsHelp(final String content, final boolean expectedResult) {
        when(event.getMessage()).thenReturn(message);
        when(message.getContent()).thenReturn(content);
        assertThat(listener.needsHelp(event)).isEqualTo(expectedResult);
    }

    static class Listener implements MessageListener {

        private static final String KEYWORD = "listener";

        @Override
        public String getKeyword() {
            return KEYWORD;
        }

        @Override
        public String getPrefix() {
            return "!gg " + getKeyword();
        }

        @Override
        public void showHelp(final MessageCreateEvent event) {
        }

        @Override
        public void process(final MessageCreateEvent event) {
        }
    }
}
