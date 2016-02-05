package microsys.config.service;

import java.util.Map;
import java.util.Optional;

/**
 * Defines the interface required for managing the dynamic system configuration.
 */
public interface ConfigService {
    /**
     * @return all the available configuration values
     * @throws ConfigServiceException if there is a problem retrieving or storing the dynamic system configuration
     * information
     */
    Map<String, String> getAll() throws ConfigServiceException;

    /**
     * @param key the configuration key for which a configuration value will be retrieved
     * @return the requested configuration value, possibly empty if the specified configuration key is not recognized
     * @throws ConfigServiceException if there is a problem retrieving or storing the dynamic system configuration
     * information
     */
    Optional<String> get(String key) throws ConfigServiceException;

    /**
     * @param key the configuration key to add (or update) to the dynamic system configuration
     * @param value the new value for the specified configuration key
     * @return the old value for the specified configuration key, possibly empty if the configuration key had no
     * previous value
     * @throws ConfigServiceException if there is a problem retrieving or storing the dynamic system configuration
     * information
     */
    Optional<String> set(String key, String value) throws ConfigServiceException;

    /**
     * @param key the configuration key to delete from the dynamic system configuration
     * @return the old value for the specified configuration key, possibly empty if the configuration key had no
     * previous value
     * @throws ConfigServiceException if there is a problem removing or storing the dynamic system configuration
     * information
     */
    Optional<String> unset(String key) throws ConfigServiceException;
}
