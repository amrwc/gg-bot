package dev.amrw.ggbot.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class ReadinessControllerTest {

    @InjectMocks
    private ReadinessController controller;

    @Test
    @DisplayName("Should have determined readiness of the application")
    void shouldHaveDeterminedReadiness() {
        assertThat(controller.ready().getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
