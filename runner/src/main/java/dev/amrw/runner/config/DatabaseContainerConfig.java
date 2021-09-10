package dev.amrw.runner.config;

import lombok.Data;

/**
 * Database container configuration POJO.
 */
@Data
public class DatabaseContainerConfig {

    private String repoTag;
    private String name;
    private int port;
}
