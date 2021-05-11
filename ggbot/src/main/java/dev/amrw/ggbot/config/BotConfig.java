package dev.amrw.ggbot.config;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.awt.Color;

/**
 * Bot configuration.
 */
@Data
@AllArgsConstructor
public class BotConfig {

    @Getter(AccessLevel.NONE)
    private String authToken;
    private Color embedColour;
    private String trigger;

    /** @return {@link #authToken} if not blank or empty string */
    public String getAuthToken() {
        return StringUtils.defaultIfBlank(authToken, "");
    }
}
