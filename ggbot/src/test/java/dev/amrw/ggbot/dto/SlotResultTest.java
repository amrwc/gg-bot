package dev.amrw.ggbot.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SlotResultTest {

    private SlotResult slotResult;

    @BeforeEach
    void beforeEach() {
        slotResult = new SlotResult();
    }

    @Test
    @DisplayName("Should have calculated net profit")
    void shouldHaveCalculatedNetProfit() {
        slotResult.setBet(50L);
        slotResult.setCreditsWon(200L);
        assertThat(slotResult.getNetProfit()).isEqualTo(150L);
    }
}
