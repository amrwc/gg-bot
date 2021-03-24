package dev.amrw.bin.config;

import lombok.Getter;
import lombok.Setter;

/**
 * Spring configuration POJO.
 */
@Getter
@Setter
public class SpringConfig {

    private String port;
    private String debugPort;
}
