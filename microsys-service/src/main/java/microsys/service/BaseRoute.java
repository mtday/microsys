package microsys.service;

import com.typesafe.config.Config;

import spark.Route;

import java.util.Objects;

/**
 * The base class for routes in this system.
 */
public abstract class BaseRoute implements Route {
    private final Config config;

    /**
     * @param config the static system configuration information
     */
    public BaseRoute(final Config config) {
        this.config = Objects.requireNonNull(config);
    }

    /**
     * @return the static system configuration information
     */
    public Config getConfig() {
        return this.config;
    }
}
