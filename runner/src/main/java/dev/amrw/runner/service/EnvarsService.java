package dev.amrw.runner.service;

import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;

public class EnvarsService {

    private static EnvarsService INSTANCE;

    private EnvarsService() {
    }

    public static EnvarsService getInstance() {
        return INSTANCE == null
                ? (INSTANCE = new EnvarsService())
                : INSTANCE;
    }

    public Map<String, String> getEnv() {
        return System.getenv();
    }

    public boolean verifyEnvars(final List<String> requiredEnvars) {
        final var envars = getEnv();
        return requiredEnvars.stream().allMatch(requiredEnvar ->
                envars.containsKey(requiredEnvar) && StringUtils.isNotBlank(envars.get(requiredEnvar)));
    }
}
