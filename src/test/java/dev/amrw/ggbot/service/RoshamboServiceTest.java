package dev.amrw.ggbot.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RoshamboServiceTest {

    @Mock
    private UserCreditsService userCreditsService;
    @InjectMocks
    private RoshamboService service;

    @Test
    @DisplayName("Should have calculated winnings")
    void shouldHaveCalculatedWinnings() {
        
    }
}
