package dev.amrw.ggbot.service;

import dev.amrw.ggbot.model.User;
import dev.amrw.ggbot.model.UserCredit;
import dev.amrw.ggbot.repository.UserCreditsRepository;
import org.javacord.api.event.message.MessageCreateEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.apache.commons.lang3.RandomUtils.nextInt;
import static org.apache.commons.lang3.RandomUtils.nextLong;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserCreditsServiceTest {

    @Mock
    private UsersService usersService;
    @Mock
    private UserCreditsRepository userCreditsRepository;
    @InjectMocks
    private UserCreditsService service;

    @Mock
    private MessageCreateEvent event;
    @Mock
    private User user;
    @Mock
    private UserCredit userCredit;

    @Test
    @DisplayName("Should have fetched an existing user credit using MessageAuthor")
    void shouldHaveFetchedExistingUserCreditUsingMessageAuthor() {
        when(usersService.getOrCreateUser(event)).thenReturn(user);
        when(userCreditsRepository.findUserCreditByUser(user)).thenReturn(Optional.of(userCredit));
        assertThat(service.getOrCreateUserCredit(event)).isEqualTo(userCredit);
    }

    @Test
    @DisplayName("Should have fetched an existing user credit")
    void shouldHaveFetchedExistingUserCreditUsingUser() {
        when(userCreditsRepository.findUserCreditByUser(user)).thenReturn(Optional.of(userCredit));
        assertThat(service.getOrCreateUserCredit(user)).isEqualTo(userCredit);
        verifyNoMoreInteractions(userCreditsRepository);
    }

    @Test
    @DisplayName("Should have created a new user credit when there was no match")
    void shouldHaveCreatedNewUserCreditUsingUser() {
        when(userCreditsRepository.findUserCreditByUser(user)).thenReturn(Optional.empty());
        when(userCreditsRepository.save(any(UserCredit.class))).thenReturn(userCredit);

        assertThat(service.getOrCreateUserCredit(user)).isEqualTo(userCredit);

        final var userCreditCaptor = ArgumentCaptor.forClass(UserCredit.class);
        verify(userCreditsRepository).save(userCreditCaptor.capture());
        assertThat(userCreditCaptor.getValue().getUser()).isEqualTo(user);
        assertThat(userCreditCaptor.getValue().getCredits()).isEqualTo(0L);
        verifyNoMoreInteractions(userCreditsRepository);
    }

    @Test
    @DisplayName("Should have got the user's current balance")
    void shouldHaveGotCurrentBalance() {
        final var currentBalance = nextLong();
        when(usersService.getOrCreateUser(event)).thenReturn(user);
        when(userCreditsRepository.findUserCreditByUser(user)).thenReturn(Optional.of(userCredit));
        when(userCredit.getCredits()).thenReturn(currentBalance);
        assertThat(service.getCurrentBalance(event)).isEqualTo(currentBalance);
    }

    @ParameterizedTest
    @CsvSource({
            Long.MAX_VALUE + ", 100",
            Long.MIN_VALUE + ", -100",
    })
    @DisplayName("Should have handled Long overflow when adding credits and made sure the new credits aren't negative")
    void shouldHaveHandledLongOverflowWhenAddingCredits(final Long currentCredits, final Long credits) {
        when(usersService.getOrCreateUser(event)).thenReturn(user);
        when(userCreditsRepository.findUserCreditByUser(user)).thenReturn(Optional.of(userCredit));
        when(userCredit.getCredits()).thenReturn(currentCredits);

        assertThat(service.addCredits(event, credits)).isEqualTo(currentCredits);

        verify(userCredit).setCredits(Math.max(currentCredits, 0L));
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    @DisplayName("Should have added credits and ensured the new credits aren't negative")
    void shouldHaveAddedCredits(final boolean negativeBalance) {
        final long currentCredits;
        final long credits;
        final long newCredits;
        if (negativeBalance) {
            currentCredits = nextInt() * -1L;
            credits = -100L;
            newCredits = 0L;
        } else {
            currentCredits = nextInt();
            credits = 100L;
            newCredits = currentCredits + credits;
        }
        when(usersService.getOrCreateUser(event)).thenReturn(user);
        when(userCreditsRepository.findUserCreditByUser(user)).thenReturn(Optional.of(userCredit));
        when(userCredit.getCredits()).thenReturn(currentCredits).thenReturn(newCredits);

        assertThat(service.addCredits(event, credits)).isEqualTo(newCredits);

        verify(userCredit).setCredits(newCredits);
    }
}
