package dev.amrw.ggbot.helper;

import dev.amrw.ggbot.dto.GameVerdict;
import dev.amrw.ggbot.dto.RoshamboRequest;
import dev.amrw.ggbot.dto.RoshamboResult;
import dev.amrw.ggbot.dto.RoshamboShape;
import dev.amrw.ggbot.util.DiscordMessageUtil;
import dev.amrw.ggbot.util.ResourceUtil;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.message.MessageCreateEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.FileInputStream;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;

import static org.apache.commons.lang3.RandomUtils.nextLong;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoshamboListenerHelperTest {

    @Mock
    private DiscordMessageUtil messageUtil;
    @Mock
    private ResourceUtil resourceUtil;
    @InjectMocks
    private RoshamboListenerHelper helper;

    @Mock
    private MessageCreateEvent event;
    @Mock
    private TextChannel channel;
    @Mock
    private RoshamboRequest request;
    @Mock
    private RoshamboResult result;
    @Mock
    private EmbedBuilder embedBuilder;
    @Mock
    private FileInputStream inputStream;
    @Mock
    private CompletableFuture<Message> future;

    @ParameterizedTest
    @EnumSource(GameVerdict.class)
    @DisplayName("Should have suspensefully displayed the result message")
    @SuppressWarnings("unchecked")
        // For `BiConsumer.class`
    void shouldHaveDisplayedResultSuspensefully(final GameVerdict verdict) {
        final Long creditsWon = nextLong();
        final Long netProfit = nextLong();
        final Long currentBalance = nextLong();

        when(request.getShape()).thenReturn(RoshamboShape.SCISSORS);
        when(result.getVerdict()).thenReturn(verdict);
        when(result.getShape()).thenReturn(RoshamboShape.ROCK);
        when(result.getCreditsWon()).thenReturn(creditsWon);
        when(result.getNetProfit()).thenReturn(netProfit);
        when(result.getCurrentBalance()).thenReturn(currentBalance);

        when(messageUtil.buildInfo(event, "Roshambo")).thenReturn(embedBuilder);
        when(embedBuilder.addField("Result", buildExpectedResultField(verdict))).thenReturn(embedBuilder);
        when(embedBuilder.addInlineField("Credits won", creditsWon.toString())).thenReturn(embedBuilder);
        when(embedBuilder.addInlineField("Net profit", netProfit.toString())).thenReturn(embedBuilder);
        when(embedBuilder.addInlineField("Current balance", currentBalance.toString())).thenReturn(embedBuilder);

        when(resourceUtil.getResourceAsStream(eq(RoshamboListenerHelper.class), anyString())).thenReturn(inputStream);
        when(event.getChannel()).thenReturn(channel);
        when(channel.sendMessage(any(FileInputStream.class), anyString())).thenReturn(future);
        when(future.whenCompleteAsync(any(BiConsumer.class), any(Executor.class))).thenReturn(future);
        when(future.whenCompleteAsync(any(BiConsumer.class))).thenReturn(future);

        helper.displayResultSuspensefully(event, request, result);

        verify(channel).sendMessage(eq(inputStream), anyString());
        verify(future).whenCompleteAsync(any(BiConsumer.class), any(Executor.class));
        verify(future, times(3)).whenCompleteAsync(any(BiConsumer.class));
    }

    private String buildExpectedResultField(final GameVerdict verdict) {
        final var operator = GameVerdict.WIN.equals(verdict) ? ">" : GameVerdict.DRAW.equals(verdict) ? "==" : "<";
        return String.format("%s %s %s\n**-- YOU %s --**",
                RoshamboShape.SCISSORS.getEmoji(),
                operator,
                RoshamboShape.ROCK.getEmoji(),
                verdict.getPastTense()
        );
    }
}
