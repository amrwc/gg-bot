package dev.amrw.ggbot.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.apache.commons.lang3.RandomUtils.nextLong;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SlotServiceTest {

    @Spy
    @InjectMocks
    private SlotService service;

    @Test
    @DisplayName("Should have played a game of slots")
    void shouldHavePlayed() {
        final var bet = nextLong();
        final var creditsWon = nextLong();
        when(service.calculateWinnings(eq(bet), anyString())).thenReturn(creditsWon);

        final var result = service.play(bet);

        assertThat(result.getBet()).isEqualTo(bet);
        assertThat(result.getCreditsWon()).isEqualTo(creditsWon);
    }

    @ParameterizedTest
    @CsvSource({
            "'🥇🥇🥇', 250",
            "'💎💎💎', 300",
            "'💯💯💯', 400",
            "'💵💵💵', 700",
            "'💰💰💰', 1500",
            "'🥇🥇❔', 50",
            "'❔🥇🥇', 50",
            "'💎💎❔', 200",
            "'❔💎💎', 200",
            "'💯💯❔', 200",
            "'❔💯💯', 200",
            "'💵💵❔', 350",
            "'❔💵💵', 350",
            "'🥇💎💯', 0",
    })
    @DisplayName("Should have accurately calculated the winnings")
    void shouldHaveCalculatedWinnings(final String roll, final long expectedResult) {
        assertThat(service.calculateWinnings(100L, roll)).isEqualTo(expectedResult);
    }
}
