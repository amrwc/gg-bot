package dev.amrw.bin.config;

import lombok.Getter;
import lombok.Setter;

/**
 * Database configuration POJO.
 */
@Getter
@Setter
public class DatabaseConfig {

    private String port;
    private String testPort;
    private String dockerImage;
    private String databaseContainer;
    private String databaseTestContainer;
    private String pgAdminImage;
    private String pgAdminContainer;
    private String pgAdminPort;
}
