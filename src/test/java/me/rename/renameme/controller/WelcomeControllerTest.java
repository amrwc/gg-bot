package me.rename.renameme.controller;

import me.rename.renameme.model.WelcomeMessage;
import me.rename.renameme.request.WelcomeMessageRequest;
import me.rename.renameme.service.WelcomeMessageService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.Optional;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WelcomeControllerTest {

    @Mock
    private WelcomeMessageService service;
    @InjectMocks
    private WelcomeController controller;

    @Mock
    private WelcomeMessage welcomeMessage;
    @Mock
    private WelcomeMessageRequest welcomeMessageRequest;
    @Captor
    private ArgumentCaptor<String> stringCaptor;

    @NullSource
    @ParameterizedTest
    @ValueSource(longs = {321L})
    @DisplayName("Should not have welcomed when the WelcomeMessage has not been found")
    void shouldNotHaveWelcomed(final Long id) {
        when(service.findWelcomeMessageById(anyLong())).thenReturn(Optional.empty());
        final var result = controller.welcome(Optional.ofNullable(id));
        assertThat(result.getBody()).isNull();
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @NullSource
    @ParameterizedTest
    @ValueSource(longs = {321L})
    @DisplayName("Should have welcomed when the WelcomeMessage has been found")
    void shouldHaveWelcomed(final Long id) {
        when(service.findWelcomeMessageById(anyLong())).thenReturn(Optional.of(welcomeMessage));
        final var result = controller.welcome(Optional.ofNullable(id));
        assertThat(result.getBody()).isEqualTo(welcomeMessage);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("Should have persisted the new WelcomeMessage")
    void shouldHaveAddedWelcome() {
        final var content = randomAlphanumeric(16);
        when(welcomeMessageRequest.getContent()).thenReturn(content);
        when(service.createWelcomeMessage(anyString())).thenReturn(welcomeMessage);
        final var result = controller.addWelcome(welcomeMessageRequest);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        verify(service).createWelcomeMessage(stringCaptor.capture());
        assertThat(stringCaptor.getValue()).isEqualTo(content);
    }
}
