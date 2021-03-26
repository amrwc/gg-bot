package dev.amrw.bin.config;

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
    private List<String> command;
}
