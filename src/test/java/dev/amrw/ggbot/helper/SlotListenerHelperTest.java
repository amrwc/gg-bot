package dev.amrw.ggbot.helper;

import dev.amrw.ggbot.dto.SlotResult;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.message.MessageCreateEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SlotListenerHelperTest {

    @InjectMocks
    private SlotListenerHelper helper;

    @Mock
    private MessageCreateEvent event;
    @Mock
    private SlotResult slotResult;
    @Mock
    private TextChannel channel;
    @Mock
    private CompletableFuture<Message> future;

    @Test
    @SuppressWarnings("unchecked") // For `BiConsumer.class`
    @DisplayName("Should have suspensefully displayed the result message")
    void shouldHaveDisplayedResultSuspensefully() {
        when(slotResult.getPayline()).thenReturn("💯💯💯");
        when(event.getChannel()).thenReturn(channel);
        when(channel.sendMessage(any(EmbedBuilder.class))).thenReturn(future);
        when(future.whenCompleteAsync(any(BiConsumer.class), any(Executor.class))).thenReturn(future);

        helper.displayResultSuspensefully(event, slotResult);

        verify(future, times(3)).whenCompleteAsync(any(BiConsumer.class), any(Executor.class));
    }
}
