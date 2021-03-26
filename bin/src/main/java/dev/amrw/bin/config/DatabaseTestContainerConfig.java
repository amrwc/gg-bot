package dev.amrw.bin.config;

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
