package dev.amrw.runner.config;

/**
 * Environment variable names.
 */
public class Envar {

    // The following envars, when passed into a Docker container, are equivalent to setting `spring.datasource.*`
    // values `application.yml`.
    /** Database URL. */
    public static final String SPRING_DATASOURCE_URL = "SPRING_DATASOURCE_URL";
    /** Database username. */
    public static final String SPRING_DATASOURCE_USERNAME = "SPRING_DATASOURCE_USERNAME";
    /** Database password. */
    public static final String SPRING_DATASOURCE_PASSWORD = "SPRING_DATASOURCE_PASSWORD";
}
