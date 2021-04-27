package dev.amrw.runner.config;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Build image config POJO.
 */
@Getter
@Setter
public class BuildImageConfig {

    private String name;
    private String user;
    private String volume;
    private String gradleCachePath;
    private String libsPath;
    private String libsArchiveName;
    private List<String> command;
}
