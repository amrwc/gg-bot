package dev.amrw.ggbot.service;

import dev.amrw.ggbot.model.User;
import dev.amrw.ggbot.model.UserCredit;
import dev.amrw.ggbot.repository.UserCreditsRepository;
import org.javacord.api.entity.message.MessageAuthor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

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
    private MessageAuthor messageAuthor;
    @Mock
    private User user;
    @Mock
    private UserCredit userCredit;

    @Test
    @DisplayName("Should have fetched an existing user credit using MessageAuthor")
    void shouldHaveFetchedExistingUserCreditUsingMessageAuthor() {
        when(usersService.getOrCreateUser(messageAuthor)).thenReturn(user);
        when(userCreditsRepository.findUserCreditByUser(user)).thenReturn(Optional.of(userCredit));
        assertThat(service.getOrCreateUserCredit(messageAuthor)).isEqualTo(userCredit);
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
        when(usersService.getOrCreateUser(messageAuthor)).thenReturn(user);
        when(userCreditsRepository.findUserCreditByUser(user)).thenReturn(Optional.of(userCredit));
        when(userCredit.getCredits()).thenReturn(currentBalance);
        assertThat(service.getCurrentBalance(messageAuthor)).isEqualTo(currentBalance);
    }
}