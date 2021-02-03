package dev.amrw.ggbot.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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

    @ParameterizedTest
    @CsvSource({
            "4, 20",
            "19, 5",
            "25, 0",
    })
    @DisplayName("Should have got duration until new daily credits can be claimed")
    void shouldHaveGotDurationUntilNextDaily(final long elapsed, final long left) {
        userCredit.setLastDaily(Date.from(Instant.now().minus(elapsed, ChronoUnit.HOURS)));
        assertThat(userCredit.getDurationUntilNextDaily()).isLessThanOrEqualTo(Duration.ofHours(left));
    }

    @ParameterizedTest
    @CsvSource({
            "4, '19:59:'",
            "19, '04:59:'",
            "25, '00:00:'",
    })
    @DisplayName("Should have got time left until new daily credits can be claimed")
    void shouldHaveGotTimeLeftUntilNextDaily(final long elapsed, final String timeLeftPrefix) {
        userCredit.setLastDaily(Date.from(Instant.now().minus(elapsed, ChronoUnit.HOURS)));
        assertThat(userCredit.getTimeLeftUntilNextDaily()).contains(timeLeftPrefix);
        assertThat(userCredit.getTimeLeftUntilNextDaily()).matches(timeLeftPrefix + "\\d\\d");
    }
}
