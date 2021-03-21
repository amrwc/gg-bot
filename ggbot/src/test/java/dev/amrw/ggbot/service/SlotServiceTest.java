package dev.amrw.ggbot.service;

import dev.amrw.ggbot.dto.Error;
import dev.amrw.ggbot.dto.GameRequest;
import dev.amrw.ggbot.dto.GameVerdict;
import dev.amrw.ggbot.dto.SlotResult;
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

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.apache.commons.lang3.RandomUtils.nextInt;
import static org.apache.commons.lang3.RandomUtils.nextLong;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SlotServiceTest {

    @Mock
    private UserCreditsService userCreditsService;
    @Spy
    @InjectMocks
    private SlotService service;

    @Mock
    private MessageCreateEvent event;

    @Test
    @DisplayName("Should not have played a game of slots with insufficient credits")
    void shouldNotHavePlayed() {
        final var bet = nextLong();
        final var currentBalance = bet / 2;
        when(userCreditsService.getCurrentBalance(event)).thenReturn(currentBalance);

        final var result = service.play(new GameRequest(bet, event, null));

        final var expectedResult = new SlotResult();
        expectedResult.setBet(bet);
        expectedResult.setHasPlayed(false);
        expectedResult.setCurrentBalance(currentBalance);
        expectedResult.setError(Error.INSUFFICIENT_CREDITS);
        assertThat(result).usingRecursiveComparison().isEqualTo(expectedResult);
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
        when(userCreditsService.getCurrentBalance(event)).thenReturn(currentBalance);
        doReturn(payline).when(service).spin();
        doReturn(winnings).when(service).calculateWinnings(bet, payline);
        when(userCreditsService.addCredits(event, winnings - bet)).thenReturn(newBalance);

        final var result = service.play(new GameRequest(bet, event, null));

        final var expectedResult = new SlotResult();
        expectedResult.setBet((long) bet);
        expectedResult.setHasPlayed(true);
        expectedResult.setVerdict(GameVerdict.WIN);
        expectedResult.setCreditsWon(winnings);
        expectedResult.setCurrentBalance(newBalance);
        expectedResult.setPayline(payline);
        assertThat(result).usingRecursiveComparison().isEqualTo(expectedResult);
        verifyNoMoreInteractions(userCreditsService);
    }

    @Test
    @DisplayName("Should have played a game of slots")
    void shouldHavePlayed() {
        final var bet = nextInt();
        final var currentBalance = nextLong(bet, Long.MAX_VALUE);
        final var newBalance = nextLong();
        when(userCreditsService.getCurrentBalance(event)).thenReturn(currentBalance);
        when(userCreditsService.addCredits(eq(event), anyLong())).thenReturn(newBalance);

        final var result = service.play(new GameRequest(bet, event, null));

        final var expectedResult = new SlotResult();
        expectedResult.setBet((long) bet);
        expectedResult.setHasPlayed(true);
        expectedResult.setCurrentBalance(newBalance);
        assertThat(result)
                .usingRecursiveComparison()
                .ignoringFields("verdict", "creditsWon", "payline")
                .isEqualTo(expectedResult);
        assertThat(result.getCreditsWon()).isNotNegative();
        assertThat(result.getPayline()).isNotEmpty();
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

    @ParameterizedTest
    @CsvSource({
            "'❔🥇🥇', 1",
            "'🥇❔🥇', 0",
            "'🥇🥇❔', 1",
            "'🥇🥇🥇', 3",
            "'❔💎💎', 2",
            "'💎❔💎', 0",
            "'💎💎❔', 2",
            "'💎💎💎', 3",
            "'❔💯💯', 2",
            "'💯❔💯', 0",
            "'💯💯❔', 2",
            "'💯💯💯', 4",
            "'❔💵💵', 4",
            "'💵❔💵', 0",
            "'💵💵❔', 4",
            "'💵💵💵', 7",
            "'❔💰💰', 7",
            "'💰❔💰', 0",
            "'💰💰❔', 7",
            "'💰💰💰', 15",
            "'🥇💎💯', 0",
    })
    @DisplayName("Should have accurately calculated small winnings")
    void shouldHaveRoundedSmallWinnings(final String payline, final long expectedResult) {
        assertThat(service.calculateWinnings(1L, payline)).isEqualTo(expectedResult);
    }

    @Test
    @DisplayName("Should have caught Long overflow when calculating winnings")
    void shouldHaveCaughtLongOverflowWhenCalculatingWinnings() {
        assertThat(service.calculateWinnings(Long.MAX_VALUE / 2, "💎💎💎")).isEqualTo(Long.MAX_VALUE);
    }
}
