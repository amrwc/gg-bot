package dev.amrw.bin.config;

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
    private String port;
}
