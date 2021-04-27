package dev.amrw.runner.config;

import lombok.Getter;
import lombok.Setter;

/**
 * Database test container configuration POJO.
 */
@Getter
@Setter
public class DatabaseTestContainerConfig {

    private String repoTag;
    private String name;
    private String port;
}
