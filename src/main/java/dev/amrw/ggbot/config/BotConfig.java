package dev.amrw.ggbot.config;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.awt.Color;

/**
 * POJO holding bot configuration as a singleton.
 */
@Data
@AllArgsConstructor
public class BotConfig {

    private String authToken;
    private Color embedColour;
    private String trigger;
}
