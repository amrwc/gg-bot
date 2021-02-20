package dev.amrw.ggbot.service;

import dev.amrw.ggbot.dto.*;
import dev.amrw.ggbot.dto.Error;
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

import static org.apache.commons.lang3.RandomUtils.nextInt;
import static org.apache.commons.lang3.RandomUtils.nextLong;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoshamboServiceTest {

    @Mock
    private UserCreditsService userCreditsService;
    @Spy
    @InjectMocks
    private RoshamboService service;

    @Mock
    private MessageCreateEvent event;

    @ParameterizedTest
    @CsvSource({
            "ROCK, ROCK, DRAW",
            "ROCK, PAPER, LOSS",
            "ROCK, SCISSORS, WIN",

            "PAPER, ROCK, WIN",
            "PAPER, PAPER, DRAW",
            "PAPER, SCISSORS, LOSS",

            "SCISSORS, ROCK, LOSS",
            "SCISSORS, PAPER, WIN",
            "SCISSORS, SCISSORS, DRAW",
    })
    void shouldHaveDeterminedVerdictCorrectly(
            final RoshamboShape requestedShape,
            final RoshamboShape randomisedShape,
            final GameVerdict expectedVerdict
    ) {
        assertThat(RoshamboService.RULES.get(requestedShape).get(randomisedShape)).isEqualTo(expectedVerdict);
    }

    @Test
    @DisplayName("Should not have played when the user has insufficient credits")
    void shouldNotHavePlayedWithInsufficientBalance() {
        final var bet = nextLong();
        final var request = new RoshamboRequest();
        request.setBet(bet);
        request.setEvent(event);
        final var currentBalance = bet / 2;
        when(userCreditsService.getCurrentBalance(event)).thenReturn(currentBalance);

        final var result = service.play(request);

        final var expectedResult = new RoshamboResult();
        expectedResult.setBet(request.getBet());
        expectedResult.setHasPlayed(false);
        expectedResult.setCurrentBalance(currentBalance);
        expectedResult.setError(Error.INSUFFICIENT_CREDITS);
        assertThat(result).usingRecursiveComparison().isEqualTo(expectedResult);
    }

    @Test
    @DisplayName("Should have played a mocked game of Roshambo where the result is known")
    void shouldHavePlayedMocked() {
        final var bet = (long) nextInt();
        final var request = new RoshamboRequest();
        request.setBet(bet);
        request.setEvent(event);
        request.setShape(RoshamboShape.ROCK);
        final var currentBalance = bet * 3;
        final var winnings = bet * 2;
        final var newBalance = currentBalance + winnings - bet;

        when(userCreditsService.getCurrentBalance(event)).thenReturn(currentBalance);
        doReturn(RoshamboShape.SCISSORS).when(service).randomiseShape();
        doReturn(winnings).when(service).calculateWinnings(bet, GameVerdict.WIN);
        when(userCreditsService.addCredits(event, winnings - bet)).thenReturn(newBalance);

        final var result = service.play(request);

        final var expectedResult = new RoshamboResult();
        expectedResult.setBet(request.getBet());
        expectedResult.setHasPlayed(true);
        expectedResult.setVerdict(GameVerdict.WIN);
        expectedResult.setCreditsWon(winnings);
        expectedResult.setCurrentBalance(newBalance);
        expectedResult.setShape(RoshamboShape.SCISSORS);
        assertThat(result).usingRecursiveComparison().isEqualTo(expectedResult);
        verifyNoMoreInteractions(userCreditsService);
    }

    @Test
    @DisplayName("Should have played a game of Roshambo")
    void shouldHavePlayed() {
        final var bet = (long) nextInt();
        final var request = new RoshamboRequest();
        request.setBet(bet);
        request.setEvent(event);
        request.setShape(RoshamboShape.ROCK);
        final var currentBalance = bet * 3;
        final var winnings = bet * 2;
        final var newBalance = currentBalance + winnings - bet;

        when(userCreditsService.getCurrentBalance(event)).thenReturn(currentBalance);
        when(userCreditsService.addCredits(eq(event), anyLong())).thenReturn(newBalance);

        final var result = service.play(request);

        final var expectedResult = new RoshamboResult();
        expectedResult.setBet(request.getBet());
        expectedResult.setHasPlayed(true);
        expectedResult.setCurrentBalance(newBalance);
        assertThat(result)
                .usingRecursiveComparison()
                .ignoringFields("verdict", "creditsWon", "shape")
                .isEqualTo(expectedResult);
        assertThat(result.getCreditsWon()).isNotNegative();
        verifyNoMoreInteractions(userCreditsService);
    }

    @ParameterizedTest
    @CsvSource({
            "100, WIN, 200",
            "100, DRAW, 100",
            "100, LOSS, 0",
    })
    @DisplayName("Should have calculated winnings")
    void shouldHaveCalculatedWinnings(final Long bet, final GameVerdict verdict, final Long expectedResult) {
        assertThat(service.calculateWinnings(bet, verdict)).isEqualTo(expectedResult);
    }
}
