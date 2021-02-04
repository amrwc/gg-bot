package dev.amrw.ggbot.service;

import dev.amrw.ggbot.model.User;
import dev.amrw.ggbot.model.UserCredit;
import org.javacord.api.entity.message.MessageAuthor;
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
    private MessageAuthor messageAuthor;
    @Mock
    private User user;
    @Mock
    private UserCredit userCredit;

    @Test
    @DisplayName("User who never has never had credits should have claimed daily credits")
    void newUserShouldHaveClaimedCredits() {
        when(usersService.getOrCreateUser(messageAuthor)).thenReturn(user);
        when(user.getUserCredit()).thenReturn(null);
        when(userCreditsService.getOrCreateUserCredit(user)).thenReturn(userCredit);

        assertThat(service.claimDailyCredits(messageAuthor)).isEqualTo(2_500L);

        verify(userCredit).setCredits(DailyService.DEFAULT_DAILY_CREDITS);
        verify(userCredit).setLastDaily(any(Date.class));
        verify(user).setUserCredit(userCredit);
    }

    @Test
    @DisplayName("An existing user should not have claimed credits before the configured time has elapsed")
    void existingUserShouldNotHaveClaimedCredits() {
        when(usersService.getOrCreateUser(messageAuthor)).thenReturn(user);
        when(user.getUserCredit()).thenReturn(userCredit);
        when(userCredit.canClaimDailyCredits()).thenReturn(false);

        assertThat(service.claimDailyCredits(messageAuthor)).isEqualTo(0L);

        verifyNoMoreInteractions(userCredit);
    }

    @Test
    @DisplayName("An existing user should have claimed daily credits")
    void existingUserShouldHaveClaimedCredits() {
        final var currentCredits = nextLong(0, Integer.MAX_VALUE);
        when(usersService.getOrCreateUser(messageAuthor)).thenReturn(user);
        when(user.getUserCredit()).thenReturn(userCredit);
        when(userCredit.canClaimDailyCredits()).thenReturn(true);
        when(userCredit.getCredits()).thenReturn(currentCredits);

        assertThat(service.claimDailyCredits(messageAuthor)).isEqualTo(DailyService.DEFAULT_DAILY_CREDITS);

        verify(userCredit).setCredits(DailyService.DEFAULT_DAILY_CREDITS + currentCredits);
        verify(userCredit).setLastDaily(any(Date.class));
    }
}
