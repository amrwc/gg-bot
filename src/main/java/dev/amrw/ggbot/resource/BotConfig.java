package dev.amrw.ggbot.resource;

import lombok.Data;

/**
 * Class mapping the bot configuration file's contents.
 */
@Data
public class BotConfig {

    public static final String PATH = "/bot-config.yml";

    public String authToken;
    public String trigger;
}
