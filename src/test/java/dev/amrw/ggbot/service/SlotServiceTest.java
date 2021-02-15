package dev.amrw.ggbot.service;

import dev.amrw.ggbot.dto.Error;
import dev.amrw.ggbot.dto.GameRequest;
import dev.amrw.ggbot.dto.SlotResult;
import org.javacord.api.entity.message.MessageAuthor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.apache.commons.lang3.RandomUtils.nextInt;
import static org.apache.commons.lang3.RandomUtils.nextLong;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SlotServiceTest {

    @Mock
    private UserCreditsService userCreditsService;
    @Spy
    @InjectMocks
    private SlotService service;

    @Mock
    private MessageAuthor messageAuthor;

    @Test
    @DisplayName("Should not have played a game of slots with insufficient credits")
    void shouldNotHavePlayed() {
        final var bet = nextLong();
        final var currentBalance = bet / 2;
        when(userCreditsService.getCurrentBalance(messageAuthor)).thenReturn(currentBalance);

        final var result = service.play(new GameRequest(bet, messageAuthor));

        assertThat(result)
                .usingRecursiveComparison()
                .isEqualTo(new SlotResult(bet, false, 0L, "", currentBalance, Error.INSUFFICIENT_CREDITS));
        verifyNoMoreInteractions(userCreditsService);
    }

    @Test
    @DisplayName("Should have played a game of slots")
    void shouldHavePlayed() {
        final var bet = nextInt();
        final var currentBalance = nextLong(bet, Long.MAX_VALUE);
        final var newBalance = nextLong();
        when(userCreditsService.getCurrentBalance(messageAuthor)).thenReturn(currentBalance);
        when(userCreditsService.addCredit(eq(messageAuthor), anyLong())).thenReturn(newBalance);

        final var result = service.play(new GameRequest(bet, messageAuthor));

        assertThat(result)
                .usingRecursiveComparison()
                .ignoringFields("creditsWon", "payline")
                .isEqualTo(new SlotResult(bet, true, -1L, "<placeholder>", newBalance, null));
        assertThat(result.getCreditsWon()).isNotNegative();
        assertThat(result.getPayline()).isNotEmpty();
        verifyNoMoreInteractions(userCreditsService);
    }

    @Test
    @DisplayName("Should have played a mocked game of slots where the result is known")
    void shouldHavePlayedMocked() {
        final var bet = nextInt();
        final var currentBalance = nextLong(bet, Long.MAX_VALUE);
        final var payline = randomAlphanumeric(3);
        final var winnings = nextLong();
        final var newBalance = nextLong();
        when(userCreditsService.getCurrentBalance(messageAuthor)).thenReturn(currentBalance);
        when(service.spin()).thenReturn(payline);
        when(service.calculateWinnings(bet, payline)).thenReturn(winnings);
        when(userCreditsService.addCredit(messageAuthor, winnings - bet)).thenReturn(newBalance);

        final var result = service.play(new GameRequest(bet, messageAuthor));

        assertThat(result)
                .usingRecursiveComparison()
                .isEqualTo(new SlotResult(bet, true, winnings, payline, newBalance, null));
        verifyNoMoreInteractions(userCreditsService);
    }

    @Test
    @DisplayName("Should have spun the drum")
    void shouldHaveSpun() {
        assertThat(service.spin().codePoints().toArray()).hasSize(3);
    }

    @ParameterizedTest
    @CsvSource({
            "'❔🥇🥇', 50",
            "'🥇❔🥇', 0",
            "'🥇🥇❔', 50",
            "'🥇🥇🥇', 250",
            "'❔💎💎', 200",
            "'💎❔💎', 0",
            "'💎💎❔', 200",
            "'💎💎💎', 300",
            "'❔💯💯', 200",
            "'💯❔💯', 0",
            "'💯💯❔', 200",
            "'💯💯💯', 400",
            "'❔💵💵', 350",
            "'💵❔💵', 0",
            "'💵💵❔', 350",
            "'💵💵💵', 700",
            "'❔💰💰', 700",
            "'💰❔💰', 0",
            "'💰💰❔', 700",
            "'💰💰💰', 1500",
            "'🥇💎💯', 0",
    })
    @DisplayName("Should have accurately calculated the winnings")
    void shouldHaveCalculatedWinnings(final String payline, final long expectedResult) {
        assertThat(service.calculateWinnings(100L, payline)).isEqualTo(expectedResult);
    }
}
