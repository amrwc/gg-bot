package dev.amrw.ggbot.dto;

import dev.amrw.ggbot.model.UserCredit;
import lombok.*;

import java.util.Optional;

/**
 * Outcome of daily credits claim.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DailyCreditsResult {

    private Long claimedCredits = 0L;
    private UserCredit userCredit;
    @Getter(AccessLevel.NONE)
    private Error error = null;

    public Optional<Error> getError() {
        return Optional.ofNullable(error);
    }
}
