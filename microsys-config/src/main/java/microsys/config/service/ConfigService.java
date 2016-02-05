package microsys.config.service;

import microsys.config.model.ConfigKeyValue;
import microsys.config.model.ConfigKeyValueCollection;

import java.util.Optional;
import java.util.concurrent.Future;

/**
 * Defines the interface required for managing the dynamic system configuration.
 */
public interface ConfigService {
    /**
     * @return all the available configuration values
     */
    Future<ConfigKeyValueCollection> getAll();

    /**
     * @param key the configuration key for which a configuration value will be retrieved
     * @return the requested configuration value, possibly empty if the specified configuration key is not recognized
     */
    Future<Optional<ConfigKeyValue>> get(String key);

    /**
     * @param kv the configuration key and value to add (or update) to the dynamic system configuration
     * @return the old value for the specified configuration key, possibly empty if the configuration key had no
     * previous value
     */
    Future<Optional<ConfigKeyValue>> set(ConfigKeyValue kv);

    /**
     * @param key the configuration key to delete from the dynamic system configuration
     * @return the old value for the specified configuration key, possibly empty if the configuration key had no
     * previous value
     */
    Future<Optional<ConfigKeyValue>> unset(String key);
}
