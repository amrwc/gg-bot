package dev.amrw.ggbot.helper;

import dev.amrw.ggbot.dto.SlotResult;
import dev.amrw.ggbot.util.DiscordMessageUtil;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageAuthor;
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
import java.util.function.Consumer;
import java.util.function.Predicate;

import static org.apache.commons.lang3.RandomUtils.nextLong;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SlotListenerHelperTest {

    @Mock
    private DiscordMessageUtil messageUtil;
    @InjectMocks
    private SlotListenerHelper helper;

    @Mock
    private MessageCreateEvent event;
    @Mock
    private SlotResult slotResult;
    @Mock
    private TextChannel channel;
    @Mock
    private MessageAuthor messageAuthor;
    @Mock
    private EmbedBuilder embedBuilder;
    @Mock
    private CompletableFuture<Message> future;

    @Test
    @SuppressWarnings("unchecked") // For `Predicate.class`, `Consumer.class`, and `BiConsumer.class`
    @DisplayName("Should have suspensefully displayed the result message")
    void shouldHaveDisplayedResultSuspensefully() {
        final Long netProfit = nextLong();

        when(slotResult.getPayline()).thenReturn("💯💯💯");

        when(event.getMessageAuthor()).thenReturn(messageAuthor);
        when(messageUtil.buildEmbedInfo(messageAuthor, "Slot Machine")).thenReturn(embedBuilder);
        when(embedBuilder.addField(eq("Result"), anyString())).thenReturn(embedBuilder);
        when(event.getChannel()).thenReturn(channel);
        when(channel.sendMessage(embedBuilder)).thenReturn(future);

        when(embedBuilder.updateFields(any(Predicate.class), any(Consumer.class))).thenReturn(embedBuilder);
        when(slotResult.getNetProfit()).thenReturn(netProfit);
        when(embedBuilder.addField("Net profit", netProfit.toString())).thenReturn(embedBuilder);

        when(future.whenCompleteAsync(any(BiConsumer.class), any(Executor.class))).thenReturn(future);

        helper.displayResultSuspensefully(event, slotResult);

        verify(channel).sendMessage(embedBuilder);
        verify(future, times(3)).whenCompleteAsync(any(BiConsumer.class), any(Executor.class));
    }
}
