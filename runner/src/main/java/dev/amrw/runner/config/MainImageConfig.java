package dev.amrw.runner.config;

import lombok.Data;

/**
 * Main image configuration POJO.
 */
@Data
public class MainImageConfig {

    private String name;
    private int port;
    private int debugPort;
}
