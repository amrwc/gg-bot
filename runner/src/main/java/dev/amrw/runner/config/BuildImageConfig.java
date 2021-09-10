package dev.amrw.runner.config;

import lombok.Data;

import java.util.List;

/**
 * Build image config POJO.
 */
@Data
public class BuildImageConfig {

    private String name;
    private String user;
    private String volume;
    private String gradleCachePath;
    private List<String> command;
}
