package dev.amrw.ggbot.config;

import lombok.Data;

/**
 * Class mapping the bot configuration file's contents.
 */
@Data
public class BotConfig {

    public static final String PATH = "/bot-config.yml";

    public String authToken;
}
