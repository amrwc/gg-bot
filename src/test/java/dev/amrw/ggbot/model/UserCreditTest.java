package dev.amrw.ggbot.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.sql.Date;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;

public class UserCreditTest {

    private UserCredit userCredit;

    @BeforeEach
    void beforeEach() {
        userCredit = new UserCredit();
    }

    @Test
    @DisplayName("Should have got Duration.ZERO when the `lastDaily` field is null")
    void shouldHaveGotDurationZero() {
        userCredit.setLastDaily(null);
        assertThat(userCredit.getRemainingDailyCooldown()).isEqualTo(Duration.ZERO);
    }

    @ParameterizedTest
    @CsvSource({
            "4, 20",
            "19, 5",
            "25, 0",
    })
    @DisplayName("Should have got duration until new daily credits can be claimed")
    void shouldHaveGotDurationUntilNextDaily(final long hoursElapsed, final long left) {
        setLastDailyHoursAgo(hoursElapsed);
        assertThat(userCredit.getRemainingDailyCooldown()).isLessThanOrEqualTo(Duration.ofHours(left));
    }

    @ParameterizedTest
    @CsvSource({
            "4, false",
            "19, false",
            "25, true",
    })
    @DisplayName("Should have determined whether the user can already claim new daily credits")
    void shouldHaveDeterminedWhetherCanClaimDailyCredits(final long hoursElapsed, final boolean expectedResult) {
        setLastDailyHoursAgo(hoursElapsed);
        assertThat(userCredit.canClaimDailyCredits()).isEqualTo(expectedResult);
    }

    @ParameterizedTest
    @CsvSource({
            "4, '19:59:'",
            "19, '04:59:'",
            "25, '00:00:'",
    })
    @DisplayName("Should have got time left until new daily credits can be claimed")
    void shouldHaveGotTimeLeftUntilNextDaily(final long hoursElapsed, final String timeLeftPrefix) {
        setLastDailyHoursAgo(hoursElapsed);
        assertThat(userCredit.getTimeLeftUntilNextDaily()).contains(timeLeftPrefix);
        // This test has this peculiar form to compensate for the time elapsed during runtime.
        assertThat(userCredit.getTimeLeftUntilNextDaily()).matches(timeLeftPrefix + "\\d\\d");
    }

    private void setLastDailyHoursAgo(final long hoursElapsed) {
        userCredit.setLastDaily(Date.from(Instant.now().minus(hoursElapsed, ChronoUnit.HOURS)));
    }
}
