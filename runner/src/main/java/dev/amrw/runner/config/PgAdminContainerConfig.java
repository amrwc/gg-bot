package dev.amrw.runner.config;

import lombok.Data;

/**
 * pgAdmin container configuration POJO.
 */
@Data
public class PgAdminContainerConfig {

    private String repoTag;
    private String name;
    private int port;
}
