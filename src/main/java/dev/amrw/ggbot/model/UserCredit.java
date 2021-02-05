package dev.amrw.ggbot.model;

import dev.amrw.ggbot.service.DailyService;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.time.DurationFormatUtils;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

/**
 * Entity holding information about user credits.
 */
@Data
@Entity
@NoArgsConstructor
@Table(name = "USER_CREDITS")
public class UserCredit {

    @Id
    @GeneratedValue
    @Column(name = "ID", unique = true, nullable = false, insertable = false, updatable = false)
    private UUID id;

    @NotNull
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID", referencedColumnName = "ID")
    private User user;

    @NotNull
    @PositiveOrZero
    @Column(name = "CREDITS", nullable = false)
    private Long credits = 0L;

    /** When the daily credits were successfully claimed last time. */
    @Column(name = "LAST_DAILY")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastDaily;

    /** @return duration until new daily credits can be claimed */
    public Duration getRemainingDailyCooldown() {
        if (null == lastDaily) {
            return Duration.ZERO;
        }
        final var elapsed = Duration.between(this.lastDaily.toInstant(), Instant.now());
        final var remainingDuration = DailyService.DEFAULT_COOLDOWN.minus(elapsed);
        return remainingDuration.isNegative() ? Duration.ZERO : remainingDuration;
    }

    /** @return whether the user's daily cooldown has elapsed */
    public boolean canClaimDailyCredits() {
        return Duration.ZERO.equals(this.getRemainingDailyCooldown());
    }

    /** @return time left until new daily credits can be claimed in <code>HH:mm:ss</code> format */
    public String getTimeLeftUntilNextDaily() {
        return DurationFormatUtils.formatDuration(this.getRemainingDailyCooldown().toMillis(), "HH:mm:ss", true);
    }
}
