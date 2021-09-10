package dev.amrw.runner.config;

import lombok.Data;

/**
 * Database test container configuration POJO.
 */
@Data
public class DatabaseTestContainerConfig {

    private String repoTag;
    private String name;
    private int port;
}
