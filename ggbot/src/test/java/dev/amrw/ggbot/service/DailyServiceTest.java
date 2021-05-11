package dev.amrw.ggbot.service;

import dev.amrw.ggbot.dto.DailyCreditsResult;
import dev.amrw.ggbot.dto.Error;
import dev.amrw.ggbot.model.User;
import dev.amrw.ggbot.model.UserCredit;
import org.javacord.api.event.message.MessageCreateEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;

import static org.apache.commons.lang3.RandomUtils.nextLong;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DailyServiceTest {

    @Mock
    private UsersService usersService;
    @Mock
    private UserCreditsService userCreditsService;
    @InjectMocks
    private DailyService service;

    @Mock
    private MessageCreateEvent event;
    @Mock
    private User user;
    @Mock
    private UserCredit userCredit;

    @BeforeEach
    void beforeEach() {
        when(usersService.getOrCreateUser(event)).thenReturn(user);
    }

    @Test
    @DisplayName("User who never has never had credits should have claimed daily credits")
    void newUserShouldHaveClaimedCredits() {
        when(user.getUserCredit()).thenReturn(null);
        when(userCreditsService.getOrCreateUserCredit(user)).thenReturn(userCredit);

        assertThat(service.claimDailyCredits(event)).isEqualTo(
                new DailyCreditsResult(DailyService.DEFAULT_DAILY_CREDITS, userCredit, null));

        verify(userCredit).setCredits(DailyService.DEFAULT_DAILY_CREDITS);
        verify(userCredit).setLastDaily(any(Date.class));
        verify(user).setUserCredit(userCredit);
    }

    @Test
    @DisplayName("An existing user should not have claimed credits before the configured time has elapsed")
    void existingUserShouldNotHaveClaimedCredits() {
        when(user.getUserCredit()).thenReturn(userCredit);
        when(userCredit.canClaimDailyCredits()).thenReturn(false);

        assertThat(service.claimDailyCredits(event)).isEqualTo(
                new DailyCreditsResult(0L, userCredit, Error.ALREADY_COLLECTED_DAILY));

        verifyNoMoreInteractions(userCredit);
    }

    @Test
    @DisplayName("An existing user should have claimed daily credits")
    void existingUserShouldHaveClaimedCredits() {
        final var currentCredits = nextLong(0, Integer.MAX_VALUE);
        when(user.getUserCredit()).thenReturn(userCredit);
        when(userCredit.canClaimDailyCredits()).thenReturn(true);
        when(userCredit.getCredits()).thenReturn(currentCredits);

        assertThat(service.claimDailyCredits(event)).isEqualTo(
                new DailyCreditsResult(DailyService.DEFAULT_DAILY_CREDITS, userCredit, null));

        verify(userCredit).setCredits(DailyService.DEFAULT_DAILY_CREDITS + currentCredits);
        verify(userCredit).setLastDaily(any(Date.class));
    }
}
