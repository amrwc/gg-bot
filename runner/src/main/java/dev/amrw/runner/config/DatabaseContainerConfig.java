package dev.amrw.runner.config;

import lombok.Getter;
import lombok.Setter;

/**
 * Database container configuration POJO.
 */
@Getter
@Setter
public class DatabaseContainerConfig {

    private String repoTag;
    private String name;
    private int port;
}
