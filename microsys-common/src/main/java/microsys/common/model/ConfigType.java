package microsys.common.model;

/**
 * Defines the available configuration types within this system.
 */
public enum ConfigType {
    /**
     * The static system configuration from the configuration file.
     */
    STATIC,

    /**
     * The dynamic system configuration from the configuration service.
     */
    DYNAMIC,
}
