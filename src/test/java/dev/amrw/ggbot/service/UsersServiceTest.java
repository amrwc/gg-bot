package dev.amrw.ggbot.service;

import dev.amrw.ggbot.model.User;
import dev.amrw.ggbot.repository.UsersRepository;
import org.javacord.api.entity.message.MessageAuthor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsersServiceTest {

    @Mock
    private UsersRepository repository;
    @InjectMocks
    private UsersService service;

    @Mock
    private MessageAuthor messageAuthor;
    @Mock
    private User user;
    private String discordUserId;

    @BeforeEach
    void beforeEach() {
        discordUserId = randomAlphanumeric(16);
        when(messageAuthor.getIdAsString()).thenReturn(discordUserId);
    }

    @Test
    @DisplayName("Should have fetched an existing user")
    void shouldHaveFetchedExistingUser() {
        when(repository.findByDiscordUserId(discordUserId)).thenReturn(Optional.of(user));
        assertThat(service.getOrCreateUser(messageAuthor)).isEqualTo(user);
        verifyNoMoreInteractions(repository);
    }

    @Test
    @DisplayName("Should have created new user when there was no match")
    void shouldHaveCreatedNewUser() {
        final var discordUsername = randomAlphanumeric(16);
        when(repository.findByDiscordUserId(discordUserId)).thenReturn(Optional.empty());
        when(messageAuthor.getName()).thenReturn(discordUsername);
        when(repository.save(any(User.class))).thenReturn(user);

        assertThat(service.getOrCreateUser(messageAuthor)).isEqualTo(user);

        final var userCaptor = ArgumentCaptor.forClass(User.class);
        verify(repository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getDiscordUserId()).isEqualTo(discordUserId);
        assertThat(userCaptor.getValue().getDiscordUsername()).isEqualTo(discordUsername);
        verifyNoMoreInteractions(repository);
    }
}
