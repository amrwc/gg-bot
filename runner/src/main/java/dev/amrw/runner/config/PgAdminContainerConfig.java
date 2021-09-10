package dev.amrw.runner.config;

import lombok.Getter;
import lombok.Setter;

/**
 * pgAdmin container configuration POJO.
 */
@Getter
@Setter
public class PgAdminContainerConfig {

    private String repoTag;
    private String name;
    private int port;
}
