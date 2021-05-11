package dev.amrw.runner.config;

import lombok.Getter;
import lombok.Setter;

/**
 * Main image configuration POJO.
 */
@Getter
@Setter
public class MainImageConfig {

    private String name;
    private String port;
    private String debugPort;
}
