package microsys.common.config;

import javax.annotation.Nonnull;

/**
 * Defines the static configuration keys expected to exist in the system configuration.
 */
public enum CommonConfig {
    SYSTEM_NAME,
    SYSTEM_VERSION,

    SSL_ENABLED,
    SSL_KEYSTORE_FILE,
    SSL_KEYSTORE_PASSWORD,
    SSL_TRUSTSTORE_FILE,
    SSL_TRUSTSTORE_PASSWORD,

    SERVER_THREADS_MAX,
    SERVER_THREADS_MIN,
    SERVER_TIMEOUT,
    SERVER_HOSTNAME,
    SERVER_PORT_MIN,
    SERVER_PORT_MAX,

    ZOOKEEPER_HOSTS,

    EXECUTOR_THREADS,

    SHELL_HISTORY_FILE;

    /**
     * @return the key to use when retrieving the common configuration value from the system configuration file
     */
    @Nonnull
    public String getKey() {
        return name().toLowerCase().replaceAll("_", ".");
    }
}
