package dev.amrw.ggbot.config;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * POJO holding bot configuration as a singleton.
 */
@Data
@AllArgsConstructor
public class BotConfig {

    private String authToken;
    private String embedColour;
    private String trigger;
}
