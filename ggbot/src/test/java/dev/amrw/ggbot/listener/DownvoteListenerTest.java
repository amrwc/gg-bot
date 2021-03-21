package dev.amrw.ggbot.listener;

import org.javacord.api.entity.message.Message;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.reaction.ReactionAddListener;
import org.javacord.api.util.event.ListenerManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DownvoteListenerTest {

    @InjectMocks
    private DownvoteListener listener;

    @Mock
    private MessageCreateEvent event;
    @Mock
    private Message message;
    @Mock
    private ListenerManager<ReactionAddListener> listenerManager;

    @Test
    @DisplayName("Should have added reaction listener to the given message")
    void shouldHaveAddedReactionListener() {
        when(event.getMessage()).thenReturn(message);
        when(message.addReactionAddListener(any(ReactionAddListener.class))).thenReturn(listenerManager);

        listener.onMessageCreate(event);

        verify(listenerManager).removeAfter(5, TimeUnit.MINUTES);
    }
}
